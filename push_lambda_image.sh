#!/bin/bash

# Get the current directory name dynamically
PROJECT_NAME=$(basename "$PWD")
PREFIX="dynamic-pricing-"
BASE_FUNCTION_NAME="${PROJECT_NAME/$PREFIX/}"

# Parse input parameter for environment (demo or prod)
if [ "$1" != "demo" ] && [ "$1" != "prod" ]; then
    echo "❌ Invalid environment. Please provide 'demo' or 'prod' as the argument."
    exit 1
fi

ENV=$1
echo "Using environment: $ENV"

# Set AWS profile based on environment
if [ "$ENV" == "demo" ]; then
    AWS_PROFILE="AWS_DYNAMIC_PRICING_DEMO"
    AWS_REGION="eu-south-1"
    FUNCTION_NAME="dynamic-pricing-demo-${BASE_FUNCTION_NAME}"
elif [ "$ENV" == "prod" ]; then
    AWS_PROFILE="AWS_DYNAMIC_PRICING_PROD"
    AWS_REGION="eu-south-1"
    FUNCTION_NAME="dynamic-pricing-prod-${BASE_FUNCTION_NAME}"
fi

# Function to check if Amazon Corretto 21 is installed
check_corretto() {
    # Get Java version details
    JAVA_VERSION=$(java -version 2>&1 | head -n 5)

    if [[ $JAVA_VERSION == *"OpenJDK Runtime Environment Corretto-21"* ]]; then
        echo "✅ Amazon Corretto 21 is installed."
    else
        echo "❌ Amazon Corretto 21 is not installed or not configured properly."
        exit 1
    fi
}

# Function to check if Docker is installed
check_docker_installed() {
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker is not installed or not in PATH."
        exit 1
    fi
}

# Function to check if Docker is running
check_docker_running() {
    if ! docker info &> /dev/null; then
        echo "❌ Docker is installed but not running. Please start Docker."
        exit 1
    fi
}

# Function to check if AWS CLI is installed and configured
check_aws_cli() {
    if ! command -v aws &> /dev/null; then
        echo "❌ AWS CLI is not installed. Please install it: https://aws.amazon.com/cli/"
        exit 1
    fi

    # Check if AWS CLI is configured properly
    if ! aws sts get-caller-identity --profile "$AWS_PROFILE" &> /dev/null; then
        echo "❌ AWS CLI is installed but not configured properly. Run 'aws sso login --profile $AWS_PROFILE' to set it up."
        exit 1
    fi

    echo "✅ AWS CLI is installed and configured correctly."
}

# Function to check and create ECR repository if it does not exist
create_ecr_repository() {
    # Set your ECR repository name (can be dynamic or hardcoded)
    ECR_REPO_NAME="$PROJECT_NAME"

    # Set AWS ECR registry URL
    AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --profile "$AWS_PROFILE" --output text)
    if [ $? -ne 0 ]; then
      echo "❌ Failed to retrieve AWS Account ID. Check your AWS CLI configuration."
      exit 1
    fi

    ECR_REGISTRY_URL="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

    # Ensure the repository exists in AWS ECR
    echo "🔍 Checking if ECR repository exists..."
    aws ecr describe-repositories --repository-names "$ECR_REPO_NAME" --region "$AWS_REGION" --profile "$AWS_PROFILE" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
      echo "❌ Repository $ECR_REPO_NAME does not exist in ECR. Creating repository..."
      aws ecr create-repository --repository-name "$ECR_REPO_NAME" --region "$AWS_REGION" --profile "$AWS_PROFILE"
      if [ $? -ne 0 ]; then
        echo "❌ Failed to create ECR repository. Exiting."
        exit 1
      fi
      echo "✅ ECR repository $ECR_REPO_NAME created successfully."
    else
      echo "✅ Repository $ECR_REPO_NAME already exists in ECR."
    fi
}

# Function to tag and push the Docker image to AWS ECR
push_to_ecr() {
    VERSION=$(date +'%Y%m%d%H%M%S')

    # Set your ECR repository name (can be dynamic or hardcoded)
    ECR_REPO_NAME="$PROJECT_NAME"

    # Set AWS ECR registry URL based on environment
    AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --profile "$AWS_PROFILE" --output text)
    if [ $? -ne 0 ]; then
      echo "❌ Failed to retrieve AWS Account ID. Check your AWS CLI configuration."
      exit 1
    fi

    ECR_REGISTRY_URL="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

    # Ensure the repository exists in AWS ECR
    echo "🔍 Checking if ECR repository exists..."
    aws ecr describe-repositories --repository-names "$ECR_REPO_NAME" --region "$AWS_REGION" --profile "$AWS_PROFILE" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
      echo "❌ Repository $ECR_REPO_NAME does not exist in ECR. Please create the repository"
      exit 1
    fi

    # Tag the Docker image with version
    docker tag "quarkus/$PROJECT_NAME" "$ECR_REGISTRY_URL/$ECR_REPO_NAME:$VERSION"
    if [ $? -ne 0 ]; then
      echo "❌ Failed to tag Docker image. Exiting."
      exit 1
    fi

    # Tag the Docker image with latest
    docker tag "quarkus/$PROJECT_NAME" "$ECR_REGISTRY_URL/$ECR_REPO_NAME:latest"
      if [ $? -ne 0 ]; then
        echo "❌ Failed to tag Docker image. Exiting."
        exit 1
    fi

    # Login to AWS ECR
    echo "🔑 Logging in to AWS ECR..."
    aws ecr get-login-password --profile "$AWS_PROFILE" --region "$AWS_REGION" | docker login --username AWS --password-stdin "$ECR_REGISTRY_URL"
    if [ $? -ne 0 ]; then
      echo "❌ Failed to login to AWS ECR. Check AWS CLI credentials or network."
      exit 1
    fi

    # Push the Docker image to ECR
    echo "🚀 Pushing Docker image to AWS ECR..."
    docker push "$ECR_REGISTRY_URL/$ECR_REPO_NAME:$VERSION"
    if [ $? -ne 0 ]; then
      echo "❌ Failed to push Docker image to AWS ECR. Check Docker configuration."
      exit 1
    fi

    # Push the Docker image with the 'latest' tag
    echo "🚀 Pushing Docker image with tag latest to AWS ECR..."
    docker push "$ECR_REGISTRY_URL/$ECR_REPO_NAME:latest"
    if [ $? -ne 0 ]; then
      echo "❌ Failed to push Docker image with tag latest. Check Docker configuration."
      exit 1
    fi

    # Clean the Docker image
    echo "🧹 Cleaning up old Docker images..."
    docker rmi "quarkus/$PROJECT_NAME"
    docker rmi "$ECR_REGISTRY_URL/$ECR_REPO_NAME:$VERSION"
    docker rmi "$ECR_REGISTRY_URL/$ECR_REPO_NAME:latest"

    echo "✅ Docker image pushed to AWS ECR: $ECR_REGISTRY_URL/$ECR_REPO_NAME"
}

# Function to update Lambda with the new Docker image
update_lambda() {
    # Ask for confirmation to update Lambda
    read -p "Do you want to update the Lambda function with the new Docker image? (y/n): " user_response
    if [[ "$user_response" == "y" || "$user_response" == "Y" ]]; then
        echo "🔄 Updating Lambda function..."

        # Update Lambda function with the new image from ECR
        aws lambda update-function-code \
            --function-name "$FUNCTION_NAME" \
            --image-uri "$ECR_REGISTRY_URL/$ECR_REPO_NAME:$VERSION" \
            --profile "$AWS_PROFILE" \
            --region "$AWS_REGION"

        if [ $? -ne 0 ]; then
            echo "❌ Failed to update Lambda function. Check AWS Lambda permissions or network."
            exit 1
        fi

        echo "✅ Lambda function $LAMBDA_FUNCTION_NAME updated successfully with image $ECR_REGISTRY_URL/$ECR_REPO_NAME:$VERSION."
    else
        echo "⚡ Skipping Lambda update."
    fi
}

# Run checks
check_corretto
check_docker_installed
check_docker_running
check_aws_cli

# Build Quarkus native image
echo "🚀 Building Quarkus native image..."
if ./gradlew clean build -x test -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true; then
    echo "✅ Quarkus native image created successfully!"
else
    echo "❌ Error: Failed to create the Quarkus native image."
    exit 1
fi

# Build the Docker image
echo "🐳 Building Docker image: quarkus/$PROJECT_NAME..."
if docker build -f src/main/docker/Dockerfile.native-micro -t "quarkus/$PROJECT_NAME" .; then
    echo "✅ Docker image 'quarkus/$PROJECT_NAME' built successfully!"
else
    echo "❌ Error: Failed to build the Docker image."
    exit 1
fi

# Ensure ECR repository exists (create if not)
create_ecr_repository

# Push the Docker image to AWS ECR
push_to_ecr

# Ask if the user wants to update the Lambda
update_lambda
#!/bin/bash

# Version bump script for Gradle project
# Usage: ./bump-version.sh [major|minor|patch|snapshot|release] [version]

GRADLE_PROPERTIES="gradle.properties"

if [ ! -f "$GRADLE_PROPERTIES" ]; then
    echo "Error: $GRADLE_PROPERTIES not found"
    exit 1
fi

# Get current version
CURRENT_VERSION=$(grep "^version=" "$GRADLE_PROPERTIES" | cut -d'=' -f2)
echo "Current version: $CURRENT_VERSION"

# Function to bump version
bump_version() {
    local version=$1
    local type=$2
    
    if [[ $version == *"-SNAPSHOT" ]]; then
        local base_version=${version%-SNAPSHOT}
    else
        local base_version=$version
    fi
    
    IFS='.' read -ra VERSION_PARTS <<< "$base_version"
    local major=${VERSION_PARTS[0]:-0}
    local minor=${VERSION_PARTS[1]:-0}
    local patch=${VERSION_PARTS[2]:-0}
    
    case $type in
        major)
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        minor)
            minor=$((minor + 1))
            patch=0
            ;;
        patch)
            patch=$((patch + 1))
            ;;
        snapshot)
            # Add or keep SNAPSHOT suffix
            if [[ $version != *"-SNAPSHOT" ]]; then
                version="${base_version}-SNAPSHOT"
            fi
            echo "$version"
            return
            ;;
        release)
            # Remove SNAPSHOT suffix
            echo "$base_version"
            return
            ;;
        *)
            echo "Error: Invalid bump type. Use: major, minor, patch, snapshot, or release"
            exit 1
            ;;
    esac
    
    echo "${major}.${minor}.${patch}"
}

# Parse arguments
if [ $# -eq 0 ]; then
    echo "Usage: $0 [major|minor|patch|snapshot|release] [version]"
    echo ""
    echo "Examples:"
    echo "  $0 patch              # Bump patch version: 0.0.1-SNAPSHOT -> 0.0.2-SNAPSHOT"
    echo "  $0 minor              # Bump minor version: 0.0.1-SNAPSHOT -> 0.1.0-SNAPSHOT"
    echo "  $0 major              # Bump major version: 0.0.1-SNAPSHOT -> 1.0.0-SNAPSHOT"
    echo "  $0 snapshot           # Ensure SNAPSHOT suffix"
    echo "  $0 release            # Remove SNAPSHOT suffix"
    echo "  $0 patch 1.2.3        # Set specific version"
    exit 1
fi

BUMP_TYPE=$1
SPECIFIC_VERSION=$2

if [ -n "$SPECIFIC_VERSION" ]; then
    NEW_VERSION=$SPECIFIC_VERSION
else
    NEW_VERSION=$(bump_version "$CURRENT_VERSION" "$BUMP_TYPE")
    
    # Preserve SNAPSHOT suffix unless it's a release
    if [[ "$BUMP_TYPE" != "release" ]] && [[ "$CURRENT_VERSION" == *"-SNAPSHOT" ]]; then
        NEW_VERSION="${NEW_VERSION}-SNAPSHOT"
    fi
fi

# Update gradle.properties
sed -i.bak "s/^version=.*/version=$NEW_VERSION/" "$GRADLE_PROPERTIES"
rm -f "${GRADLE_PROPERTIES}.bak"

echo "Version updated: $CURRENT_VERSION -> $NEW_VERSION"
echo ""
echo "Next steps:"
echo "  1. Review the change: git diff $GRADLE_PROPERTIES"
echo "  2. Commit: git add $GRADLE_PROPERTIES && git commit -m \"Bump version to $NEW_VERSION\""
echo "  3. Tag (if release): git tag -a v$NEW_VERSION -m \"Release v$NEW_VERSION\""


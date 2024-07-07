#!/usr/bin/env bash
set -e

# For snapshots, please specify the full version (with date and time)
cdist_version="0.1.0-20240706.110724-10"
cdist_path_version="$cdist_version"

if [ -n "${cdist_version#*-}" ]; then
  cdist_path_version="${cdist_version%%-*}-SNAPSHOT"
fi
url="https://maven.enginehub.org/repo/org/enginehub/crowdin/crowdin-distributor/$cdist_path_version/crowdin-distributor-$cdist_version-bundle.zip"
[ -d ./build ] || mkdir ./build
curl "$url" >./build/cdist.zip
(cd ./build && unzip -o cdist.zip)

# CROWDIN_DISTRIBUTOR_TOKEN is set by CI
export CROWDIN_DISTRIBUTOR_ON_CHANGE="true"
export CROWDIN_DISTRIBUTOR_PROJECT_ID="414990"
export CROWDIN_DISTRIBUTOR_MODULE="craftbook-lang"
## Full path to the source file, will be uploaded to crowdin, must already have uploaded at least once (will not create a new file)
export CROWDIN_DISTRIBUTOR_SOURCE_FILE="./craftbook-core/src/main/resources/lang/strings.json"
# Artifactory & Build Number is set by CI
export CROWDIN_DISTRIBUTOR_OPTS=""
"./build/crowdin-distributor-$cdist_path_version/bin/crowdin-distributor"

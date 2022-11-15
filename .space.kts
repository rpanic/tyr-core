job("build") {
    host("Build and push docker image") {
        dockerBuildPush {
            context = "docker"
            // path to Dockerfile relative to the project root
            // if 'file' is not specified, Docker will look for it in 'context'/Dockerfile
            file = "Dockerfile"
            // to add a raw list of additional build arguments, use
            // extraArgsForBuildCommand = listOf("...")
            // to add a raw list of additional push arguments, use
            // extraArgsForPushCommand = listOf("...")
            // image tags
            tags {
                // use current job run number as a tag - '0.0.run_number' ${"$"}JB_SPACE_EXECUTION_NUMBER
                +"space.rpanic.com/rpanic/tyr-core:1.0.${"$"}JB_SPACE_GIT_REVISION-${"$"}JB_SPACE_EXECUTION_NUMBER"
            }
        }
    }
}
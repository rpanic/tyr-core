job("build") {
    host("Build and push docker image") {
        shellScript {
            content = """
                docker build -t registry.space.rpanic.com/tyr-core:1.0.${'$'}JB_SPACE_GIT_REVISION .
            """
            //docker push registry.space.rpanic.com/tyr-core:1.0.${'$'}JB_SPACE_GIT_REVISION
        }
    }
}
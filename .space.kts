job("build") {
    host("Build and push docker image") {
        shellScript {
            content = """
                docker login registry.space.rpanic.com -u rpanic -p ${'$'}registry_pw
                docker build -t registry.space.rpanic.com/p/tyr/containers/tyr-core:${'$'}JB_SPACE_GIT_REVISION .
                docker push registry.space.rpanic.com/p/tyr/containers/tyr-core:${'$'}JB_SPACE_GIT_REVISION
            """
            //
        }
    }
}
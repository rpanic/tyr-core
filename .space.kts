job("build") {
    host("Build and push docker image") {
        shellScript {
            content = """
                docker build -t space.rpanic.com:8390/tyr-core:1.0.${'$'}JB_SPACE_GIT_REVISION .
                docker push space.rpanic.com:8390/tyr-core:1.0.${'$'}JB_SPACE_GIT_REVISION
            """.trimIndent()
        }
    }
}
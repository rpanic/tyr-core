job("build") {
    host("Build and push docker image") {
        env["PW"] = Secrets("registry-pw")
        shellScript {
            val pw = System.getEnv("PW")
            content = """
                docker login registry.space.rpanic.com -u rpanic -p $pw
                docker build -t registry.space.rpanic.com/p/tyr/containers/tyr-core:${'$'}JB_SPACE_GIT_REVISION .
                docker push registry.space.rpanic.com/p/tyr/containers/tyr-core:${'$'}JB_SPACE_GIT_REVISION
            """
            //
        }
    }
}
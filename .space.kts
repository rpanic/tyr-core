job("build") {
    host("Build and push docker image") {
        dockerBuildPush{
            context = "docker"
            // path to Dockerfile relative to the project root
            // if 'file' is not specified, Docker will look for it in 'context'/Dockerfile
            file = "Dockerfile"
            tags {
                // use current job run number as a tag - '0.0.run_number'
                +"registry.space.rpanic.com/p/tyr/containers/tyr-core:${'$'}JB_SPACE_GIT_REVISION"
            }
        }
//        env["PW"] = Secrets("registry_pw")
//        shellScript {
//            val pw = System.getenv("PW")
//            content = """
//                docker login registry.space.rpanic.com -u rpanic -p $pw
//                docker build -t registry.space.rpanic.com/p/tyr/containers/tyr-core:${'$'}JB_SPACE_GIT_REVISION .
//                docker push registry.space.rpanic.com/p/tyr/containers/tyr-core:${'$'}JB_SPACE_GIT_REVISION
//            """
//            //
//        }
    }
}
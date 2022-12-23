def call(projectName, requiredAgent='java11'){
pipeline{
        stage("Build"){
            steps{
                    echo "building ..."
                    script{
                        readData()
                   }
             }
        }
    }
}

def readData(){
    def content = readFile("hostMap.csv")
    echo "daa: ${content}"
}

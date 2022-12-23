def call(projectName, requiredAgent='java11'){
pipeline{
    agent {
        label "apac"
    }
    environment{
        def starterCommand = "rsync --progress --timeout=900 -rKLOmtzv --no-h --exclude=*.fav --exclude=*.watchlist.txt --exclude=*.raw --exclude=*.removed --exclude=*ControlTimes.cfg "
    }
    parameters{
        string(defaultValue: "", description: "What is your subscription?", name: "subscription")
        string(defaultValue: "", description: "What is the start date of the subscription?", name: "startDate")
        string(defaultValue: "", description: "What is the end date of the subscription?", name: "endDate")
        booleanParam(defaultValue: true, description: "Confirm to deploy?", name: "confirmDeploy")
        }

    stages{
        stage("Set Up"){
            steps{
                echo "seting up ..."
                }
            }
        stage("Build"){
            steps{
                    echo "building ..."
                    sh "whoami"
                    sh "pwd"
                    sh "env"
                    echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                    script{
                        starterCommand += generateExcludeCommand()
                        starterCommand += generateIncludeCommand()
                        echo "command will be execueted: ${starterCommand}"
                        mapping()
                    }
                }
            }
        stage("Test"){
            steps{
                echo "testing ..."
                //echo "Testing on target server: ${targetSever}"
                }
            }
        stage("Deploy"){
            steps{
                echo "deploying ..."
                echo "confirmDeploy: ${params.confirmDeploy}"
                script{
                    if(params.confirmDeploy == false){
                        echo "Fail to deploy"
                        }else{
                        currentBuild.result = "SUCCESS"
                        echo "Status: ${currentBuild.result}"
                        }
                    }
                }
            }
        }
    }
}

/**
* This function is used to generate the exclude command from the target server and subscription parameters
*/
def generateExcludeCommand(){
    echo "----------------------"
    return " --exclude=* aldo.int.smbc.nasdaqomx.com::data-requests/${params.subscription}/ /data01/source/data/${params.subscription}/"
}

def generateIncludeCommand(){
    def command = ""
    def start = Date.parse("yyyyMMdd", params.startDate)
    def end = Date.parse("yyyyMMdd", params.endDate)
    def allDaysList = []
    def allMonthList = []
    def allYearList = []
    while(start <= end){
        allDaysList.add(start.format('yyyyMMdd'))
        start = start.plus(1)
        if(allMonthList.isEmpty()){
            allMonthList.add(start.format('yyyyMMdd').substring(0,6))
        }else if(allMonthList[allMonthList.size() - 1] != start.format('yyyyMMdd').substring(0,6)){
            allMonthList.add(start.format('yyyyMMdd').substring(0,6))
        }
        if(allYearList.isEmpty()){
            allYearList.add(start.format('yyyyMMdd').substring(0,4))
        }else if(allYearList[allYearList.size() - 1] != start.format('yyyyMMdd').substring(0,4)){
            allYearList.add(start.format('yyyyMMdd').substring(0,4))
        }
    }
    command += " --include=track/ --include=daytot/ --include=missing_dates.txt --include=*.name --include=*.dat --include=*.cfg"
    for(year in allYearList){ 
        command += " --include=track/${year}/ --include=daytot/${year}/" 
    }

    for(timeline in allMonthList){
        String year = timeline.substring(0,4)
        String month = timeline.substring(4)

        //This line will generate --include=track/yyyy/mm/
        command += " --include=track/${year}/${month}/"
        //This line will generate --include=daytot/yyyy/mm/
        command += " --include=daytot/${year}/${month}/"
    }

    for(timeline in allDaysList){
        String year = timeline.substring(0,4)
        String month = timeline.substring(4,6)
        //This line will generate --include=track/yyyy/mm/yyyymmdd*
        command += " --include=track/${year}/${month}/${timeline}*"
    }

    for(timeline in allDaysList){
        String year = timeline.substring(0,4)
        String month = timeline.substring(4,6)
        //This line will generate  --include=daytot/yyyy/mm/yyyymmdd/
        command += " --include=daytot/${year}/${month}/${timeline}/"
        //This line will generate --include=daytot/yyyy/mm/yyyymmdd/yyyymmdd*
        command += " --include=daytot/${year}/${month}/${timeline}/${timeline}*"
    }
    return command
}

def mapping(){
    def file = readFile("hostMap.csv")
    echo "file: ${file}"
    echo "-----------------------------------------------------------------"
    file.withReader { reader ->
        while ((line = reader.readLine()) != null) {
            println "${line}"
        }
    }
}
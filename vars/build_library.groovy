    def call(String gitUrl) {
      def artiServer = Artifactory.server 'artifactory-oss'
      def rtMaven = Artifactory.newMavenBuild()
      def buildInfo = Artifactory.newBuildInfo()
      def descriptor = Artifactory.mavenDescriptor()
      buildInfo.env.capture = true

      stage('scm') {
          git gitUrl
      }

      stage('sonarqube') {

      }

      stage('maven build') {
          rtMaven.resolver releaseRepo: 'maven-remote-virtual', snapshotRepo: 'maven-remote-virtual', server: artiServer
          rtMaven.deployer releaseRepo: 'teamA-maven-dev-local', snapshotRepo: 'teamA-maven-dev-local', server: artiServer
          rtMaven.deployer.deployArtifacts = false
          descriptor.setVersion "org.wangqing:Guestbook-microservices-k8s","1.0.$BUILD_NUMBER"
          descriptor.setVersion "org.wangqing:gateway-service","1.0.$BUILD_NUMBER"
          descriptor.setVersion "org.wangqing.guestbook-microservices-k8s:discovery-service","1.0.$BUILD_NUMBER"
          descriptor.setVersion "org.wangqing.guestbook-microservices-k8s:guestbook-service","1.3.$BUILD_NUMBER"
          descriptor.setVersion "org.wangqing:zipkin-service","1.0.$BUILD_NUMBER"
          descriptor.transform()
          rtMaven.tool = 'maven363'
          rtMaven.run pom: './pom.xml', goals: 'clean install',buildInfo: buildInfo
          //rtMaven.run pom: './pom.xml', goals: 'clean package',buildInfo: buildInfo
      }

      stage('deploy to test') {
          buildInfo.env.capture = true
          rtMaven.deployer.deployArtifacts buildInfo
          artiServer.publishBuildInfo buildInfo
      }

      stage('jira') {}

      stage('unit test') {
        junit (testResults: '**/surefire-reports/**/*Test.xml')
        //def build = manager.build
        def testDatas = manager.build.getAction(hudson.tasks.junit.TestResultAction.class)
        
        if (testDatas) {
            result = testDatas.result
            JTtotal = result.getTotalCount().toString()
            JTfailed = result.getFailCount().toString()
            JTpassed = result.getPassCount().toString()
            JTskiped = result.getSkipCount().toString()
            passRate = result.getPassCount()/result.getTotalCount()*100
            passRate = passRate.toString()+"%"
        } else {
            JTtotal = '0'
            JTfailed = '0'
            JTpassed = '0'
            JTskiped = '0'
            passRate = '0'
        }
        print("Total unit test case Number: " + JTtotal)
        print("Pass unit test case Number: "+ JTpassed)
        rtMaven.deployer.addProperty("unittest.summary.total_number",JTtotal)
        rtMaven.deployer.addProperty("unittest.summary.pass_number",JTpassed)
        rtMaven.deployer.addProperty("unittest.summary.pass_rate",passRate)
        
        
    }
      stage('xray') {

      }
      stage('deploy stage') {

      }
    }
    

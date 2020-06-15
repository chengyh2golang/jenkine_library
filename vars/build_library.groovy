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
      }

      stage('deploy to test') {
          buildInfo.env.capture = true
          rtMaven.deployer.deployArtifacts buildInfo
          artiServer.publishBuildInfo buildInfo
      }

      stage('jira') {}

      stage('test') {

      }
      stage('xray') {

      }
      stage('deploy stage') {

      }
    }
    

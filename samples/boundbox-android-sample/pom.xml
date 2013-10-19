<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<parent>
		<groupId>org.boundbox</groupId>
		<artifactId>boundbox-parent</artifactId>
		<version>1.2.0</version>
		<relativePath>../..</relativePath>
	</parent>

	<artifactId>boundbox-android-sample</artifactId>
	<packaging>apk</packaging>
	<name>boundbox-android-sample</name>

	<properties>
		<!-- Android Dependencies -->
		<android.platform.version>4.3_r2</android.platform.version>
		<java.version>1.6</java.version>
		<android.sdk.version>18</android.sdk.version>

		<!-- Plugins -->
		<maven-compiler-plugin.version>2.5.1</maven-compiler-plugin.version>
		<android-maven-plugin.version>3.6.1</android-maven-plugin.version>
		<maven-deploy-plugin>2.7</maven-deploy-plugin>
	</properties>

	<dependencies>
		<dependency>
			<groupId>android</groupId>
			<artifactId>android</artifactId>
			<version>${android.platform.version}</version>
			<scope>provided</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-deploy-plugin</artifactId>
				<version>${maven-deploy-plugin}</version>
				<configuration>
					<skip>true</skip>
				</configuration>
			</plugin>
			<plugin>
				<groupId>com.jayway.maven.plugins.android.generation2</groupId>
				<artifactId>android-maven-plugin</artifactId>
				<version>${android-maven-plugin.version}</version>
				<extensions>true</extensions>
				<configuration>
					<sdk>
						<!-- platform or api level (api level 16 = platform 4.1) -->
						<platform>${android.sdk.version}</platform>
					</sdk>
				</configuration>
			</plugin>
			<plugin>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>${java.version}</source>
					<target>${java.version}</target>
				</configuration>
			</plugin>
		</plugins>
	</build>

</project>

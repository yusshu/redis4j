plugins {
    java
    id("me.champeau.jmh") version("0.6.6")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    jmh("redis.clients:jedis:3.7.0")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

jmh {
    warmupIterations.set(2)
    iterations.set(1)
    fork.set(2)
}
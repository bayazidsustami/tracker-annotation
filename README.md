# Background
- We often write almost the same code over and over again. this can make developers feel bored quickly, resulting in a lack of productivity.
- Managing these event parameters can get cumbersome really quickly since the keys and values are arbitrary and can also change rapidly depending on your analytic goals. Moreover, you need to ensure that you’re passing the correct key-value pairs or you will receive incorrect analysis.

# Solution
-   Generative Metaprogramming will help us
-   By using Code Generation will help us to tackle our problem
-   We only need write less code than before
-   By using Code Generation we also can reduce typos in write event params

# KSP as Solution
KSP is designed to hide compiler changes, minimizing maintenance efforts for processors that use it. KSP is designed not to be tied to the JVM so that it can be adapted to other platforms more easily in the future.
# Prerequisite
- java : 11
- Kotlin  : 1.6.10
- Gradle : 7.3.0
- KSP API : 1.6.10-1.0.4
- Kotlin-poet-ksp : 1.12.0

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.7"
}

rootProject.name = "psychological-testing-main"

gitHooks {

    commitMsg {
        conventionalCommits {
            types("feature", "refactoring", "fix", "codestyle", "docs", "revert", "ci", "chore")
        }
    }

//    createHooks(overwriteExisting = true)

}

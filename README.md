Gradle 插件代码可以在 build.gradle 中，buildSrc 项目中，以及独立的插件项目中编写。本文将介绍如何在一个独立的项目中使用 Java 语言编写 Gradle 插件，并发布到仓库中。

## 1 创建项目

Gradle 插件项目和普通的 Java 项目没有什么不同，普通项目是基于其它三方包进行开发，而 Gradle 插件项目基于 Gradle 的 API 进行开发。

基于 Gradle 创建一个 Java 项目，项目目录结构如下，和普通项目一样。项目源码：https://github.com/Robothy/gradle-plugin-sample

```bash
gradle-plugin-sample
|
├───build.gradle
├───settings.gradle
└───src
    ├───main
    │   ├───java
    │   └───resources
    └───test
        ├───java
        └───resources
```

引入 Gradle API 相关的 jar 包。为了方便起见，可以通过 gradle 插件 java-gradle-plugin 来引入 Java 插件，引入 Gradle API 相关依赖以及生成插件相关的描述符。

build.gradle
```java
plugins {
    id 'java-gradle-plugin'
}

group 'com.robothy'
version '1.0-SNAPSHOT'

repositories {
    mavenLocal()
    mavenCentral()
}

wrapper{
    gradleVersion = '6.7'
}
```

## 2 动手开发

项目创建好之后，就可以开始动手开发了。从项目构建角度来看，Gradle 插件是一段可重用的构建逻辑，这段逻辑能够被应用到各个项目当中。更具体来说，Gradle 插件是一个实现了 org.gradle.api.Plugin 接口的类，它被 [Project](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html#sec:projects_and_tasks) （可以认为是 build.gralde, 它本质是一个实现了 Project 接口的类）所引用。开发插件的本质就是往 build.gradle 中插入一段逻辑。

```java
void apply​(T target)
```

Plugin 是一个泛型接口，有一个抽象方法 apply，它的参数类型可以是 [Project](https://docs.gradle.org/current/dsl/org.gradle.api.Project.html), [Settings](https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html), 或者 [Gradle](https://docs.gradle.org/current/dsl/org.gradle.api.invocation.Gradle.html)。

- 类型为 Project，插件可以应用于 build.gradle；

- 类型为 Settings，插件可应用于 settings.gradle；

- 类型为 Gradle, 插件可应用于 Gradle 初始化脚本。



在应用插件时，gradle 会创建一个插件类的实例，并调用 apply 方法。因此，插件的逻辑就是 apply 方法中的代码。

一个独立的项目中可以有多个实现了 Plugin 接口的类，意味着一个项目可以包含多个插件。每一个插件都需要在 build.gradle 中添加相应的描述，java-gradle-plugin 会根据这些描述生成插件描述符（jar 包中的一个文件）。

假设要在 gradle-plugin-sample 项目中创建两个插件 hello, goodbye，需要进行如下两个步骤：

1）创建插件类

HelloPlugin.java

```java
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HelloPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("Message from hello plugin.");
    }
}
```

GoodbyePlugin.java

```java
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GoodbyePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("Message from goodbye plugin.");
    }
}
```

2）在 build.gradle 中添加描述内容

描述内容需要指定插件的 ID 和插件的入口类。

```groovy
gradlePlugin {
    plugins {
        helloPlugin {
            id = 'com.robothy.hello'
            implementationClass = 'com.robothy.HelloPlugin'
        }
        googbyePlugin{
            id = 'com.robothy.goodbye'
            implementationClass = 'com.robothy.GoodbyePlugin'
        }
    }
}
```

完成上面步骤之后，一个简单的插件就算完成了开发，接下来就可以发布和使用了。

## 3 发布插件

插件可以发布到 Maven 仓库和 Gradle 官方插件门户。

### 3.1 发布到 Maven 仓库

发布插件到 Maven 仓库和发布普通的 jar 包一样，需要用到 maven-publish 插件。要发布到远程 Maven 仓库可能需要提供认证信息，这里简单起见只发布到本地仓库。

1）在 build.gradle 文件中添加 maven-publish 插件

```groovy
plugins {
    id 'java-gradle-plugin'
    id 'maven-publish'
}
```

2）执行 `gradle publishToMavenLocal`，成功之后可以在 `~/.m2` 目录下找打发布的 jar 包。

要使用发布到 Maven 仓库中的 Gradle 插件，需要先在 settings.gradle 中指定仓库。如下代码指定了插件仓库有本地 Maven 和 Gradle 插件门户。

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```

### 3.2 发布到 Gradle 官方插件门户

我们平常使用的大部分插件来自于 Gradle 官方插件门户，开发人员注册一个 Gradle 账号之后可以将插件发布到门户，这样其他人就可以很方便地使用了。按照如下步骤发布插件，这里如果没有描述清楚可以移步 Gradle 官网文档：[How do I add my plugin to the plugin portal?](https://plugins.gradle.org/docs/submit)。

1）注册门户账户

2）创建 API Key。注册好账户就能够看见了。

3）将 API Key 添加到文件 `~/.gradle/gradle.properties`

4）使用插件发布插件 com.gradle.plugin-publish 发布插件（不是病句，只是有点绕 😔）

将 com.gradle.plugin-publish 添加到插件项目 gradle-plugin-sample 的 build.gradle 中，然后添加插件的描述信息。

```groovy
pluginBundle {
  website = 'http://www.gradle.org/'
  vcsUrl = 'https://github.com/gradle/gradle'
  description = 'Greetings from here!'
  tags = ['greetings', 'salutations']

  plugins {
    greetingsPlugin {
      // id='com.robothy.hello' 可以省略，因为在 gradlePlugin 配置块中已经有 id 信息了
      displayName = 'Hello Plugin'
    }
  }
}
```

5）使用 `gradle publishPlugins` 发布插件

如果插件信息描述正确，执行 puhlishPlugins 任务之后会打印出待审核的信息，之后就是等待了（本人发布的插件 [com.robothy.cn-repo](https://plugins.gradle.org/plugin/com.robothy.cn-repo) 经过了四五个小时就审核通过了）。

```
Publishing plugin com.robothy.cn-repo version 1.0
Thank you. Your new plugin com.robothy.cn-repo has been submitted for approval by Gradle engineers. The request should be processed within the next few days, at which point you will be contacted via email.
```

## 4 更多

### 4.1 在插件中添加任务

先自定义一个 Gradle 任务类 SayHelloTask，该任务的行为是简答的打印固定的字符串。自定义任务需要继承 DefaultTask。

```java
public class SayHelloTask {
    @TaskAction
    public void hello() {
        System.out.println("Hello, World!");
    }
}
```

然后通过 project 往项目中注册一个 SayHelloTask 的实例，任务名为 task。

```java
public class HelloPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("Message from hello plugin.");
        project.getTasks().register("hello", SayHelloTask.class);
    }
}
```

当然，也可以在使用 HelloPlugin 插件的 build.gradle 中注册任务。

```groovy
task hello(type: com.robothy.SayHelloTask)
```

重新发布插件，执行下面命令时控制台会打印出 "Hello, World!"。

```java
gradle hello
```

### 4.2 添加扩展

Gradle 插件可以往 project 中注册扩展，开发人员可以通过扩展设置一些参数值，以供其它的 Gradle Task 使用。假设我们希望在 build.gradle 中添加如下配置信息。

```
user {
    name = 'Robothy'
    country = 'China'
}
```

首先，创建一个配置信息接口，接口中只包含 getter 抽象方法。需要注意的是，返回的类型为 Property<String>，并非直接返回 String。配置信息不需要创建为 Java 类，Gradle 在运行时会通过动态代理的方式自动往代理对象中注入值。

```java
public interface User {
    Property<String> getName();
    Property<String> getCountry();
}
```

然后插件就可以往 project 中添加一个扩展了。

```java
project.getExtensions().add("user", User.class);
```

重新发布插件，此时引入了插件的项目就可以在 builde.gradle 中添加本小节开头描述的配置块了。

Gradle 任务可以通过如下方式访问到这些配置信息。

```java
User user = (User) (project.getExtensions().getByName("user"));
```

## 5 小结

本文主要介绍了如何使用纯 Java 语言在一个独立的项目中编写 Gradle 插件，插件主要通过通过传入的 project 参数访问项目，往项目的构建生命周期中插入一些逻辑或者添加配置信息。插件可以发布到私有的 Maven 仓库，也可以发布到 Gradle 插件门户。插件项目中还可以很好地封装一些 Gradle Task，定义一些配置类型。

## 6 参考内容

[1] [Build Script Basics](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html#sec:projects_and_tasks)

[2] [How do I add my plugin to the plugin portal?](https://plugins.gradle.org/docs/submit)

[3] [Developing Custom Gradle Plugins](https://docs.gradle.org/current/userguide/custom_plugins.html#note_for_plugins_published_without_java_gradle_plugin)
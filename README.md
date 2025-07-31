# 🐰 HopperMq

**HopperMq** is a lightweight and efficient Kotlin library for **RabbitMQ**, offering an intuitive API for message
queuing, event-driven communication, and structured packet handling — now powered by **KSP (Kotlin Symbol Processing)**
for fast, reflection-free packet registration.

---

## 🚀 Features

* 📦 **Simplified RabbitMQ Integration** – Abstracts connection, exchange, and queue setup.
* 🎯 **Packet-Based Messaging** – Send structured data with serialization, deserialization, and metadata.
* 🛠 **Customizable Queues & Exchanges** – Supports direct queues and exchange-based routing.
* 🎧 **Event-Driven Model** – Subscribe to packets with a simple event bus.
* 📋 **Packet Metadata Support** – Attach contextual info to each packet.
* ⚙️ **KSP Support** – Compile-time packet registration (no reflection).
* 🧱 **DSL Queue Builder** – Create queues using a clean, idiomatic builder function.

---

## 📥 Installation

### 🔧 Apply Required Plugins

In your `build.gradle.kts` or `build.gradle`, apply the **KSP plugin**:

```groovy
plugins {
   id 'org.jetbrains.kotlin.jvm' version '1.9.25'
   id 'com.google.devtools.ksp' version '1.9.25-1.0.20'
}
```

### 📦 Dependencies

#### Kotlin DSL (build.gradle.kts)

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/LevelyStudio/HopperMq")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USER")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("gg.levely.system:hoppermq-core:0.2.3")
    ksp("gg.levely.system:hoppermq-processor:0.2.3")
}
```

#### Groovy DSL (build.gradle)

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/LevelyStudio/HopperMq")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USER")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'gg.levely.system:hoppermq-core:0.2.3'
    ksp 'gg.levely.system:hoppermq-processor:0.2.3'
}
```

---

## 🔐 Authentication Setup

Since GitHub Packages requires authentication, provide a **GitHub Personal Access Token (PAT)** via:

* **Environment variables:**

  ```sh
  export GITHUB_USER=your-username
  export GITHUB_TOKEN=your-personal-access-token
  ```

* **Or `gradle.properties`:**

  ```properties
  gpr.user=your-username
  gpr.token=your-personal-access-token
  ```

---

## 🎯 Getting Started

### 1️⃣ Define Your Primary Queue

You can now initialize HopperMq directly with a queue:

```kotlin
val selfQueue = queueBuilder("YourAppName") {
    setDurable(false)
    setAutoDelete(false)
}
```

### 2️⃣ Initialize HopperMq

Instead of passing a string name, pass the queue you defined:

```kotlin
val hopperMq = HopperMq("amqp://your-rabbitmq-url", selfQueue)
```

---

### 3️⃣ Define a Packet

Each packet must be annotated with `@RabbitPacketLabel` for KSP to process it:

```kotlin
@RabbitPacketLabel
class ExamplePacket : RabbitPacket {

    lateinit var message: String

    private constructor()

    constructor(message: String) {
        this.message = message
    }

    override fun write(output: DataOutputStream) {
        output.writeUTF(message)
    }

    override fun read(input: DataInputStream) {
        message = input.readUTF()
    }
}
```

---

### 4️⃣ Register Packets via KSP

Reflection is no longer used. Register packets using the KSP-generated registry:

```kotlin
val packetRegistry = hopperMq.packetRegistry

GeneratedPacketRegistry.registerAll(packetRegistry)
```

> ✅ `GeneratedPacketRegistry` is generated automatically by the HopperMq KSP processor.

---

## 🧱 Defining Queues Using `queueBuilder`

The `queueBuilder` DSL lets you define queues in a clean, idiomatic way:

### 🔹 Private Queue

```kotlin
val exampleQueue = queueBuilder("default") {
    setDurable(false)
    setAutoDelete(false)
}
```

### 🔸 Exchange-Based Queue

```kotlin
val exchangeQueue = queueBuilder(
    queue = "globalExample",
    exchange = "global-example-shared",
    type = BuiltinExchangeType.TOPIC
) {
    setDurable(false)
    setAutoDelete(false)
}
```

Bind queues to HopperMq:

```kotlin
hopperMq.bindQueue(exampleQueue)
hopperMq.bindQueue(exchangeQueue)
```

---

## 📤 Sending and Receiving Packets

### Subscribe to a Packet

```kotlin
hopperMq.rabbitBus.subscribe(ExamplePacket::class.java) { event ->
    println("Received: ${event.message} from ${event.author ?: "Unknown"}")
}
```

### Publish a Packet

```kotlin
hopperMq.publish(exchangeQueue, ExamplePacket("Hello from HopperMq!"))
```

---

## 📌 Advanced Features

### 🧠 Packet Metadata

```kotlin
val packet = ExamplePacket("Test message")
packet.metadata["timestamp"] = System.currentTimeMillis()
```

### 🟢 Check Connection

```kotlin
if (hopperMq.isConnected) {
    println("Connected to RabbitMQ!")
}
```

### 🗑 Remove a Queue

```kotlin
hopperMq.removeQueue("queue-name")
```

### 🔒 Clean Shutdown

```kotlin
hopperMq.close()
```

---

## 📜 License

This project is licensed under the **MIT License**.

---

🐰 **Happy Messaging with HopperMq!** 🚀

---

# 🐰 HopperMq

HopperMq is a lightweight and efficient Kotlin library designed to simplify interactions with **RabbitMQ**, providing an intuitive API for message queuing, event-driven communication, and packet handling.

## 🚀 Features

- 📦 **Simplified RabbitMQ Integration** – Abstracts the complexity of managing connections, exchanges, and queues.
- 🎯 **Packet-Based Messaging** – Allows structured data communication with serialization and deserialization.
- 🛠 **Customizable Queues & Exchanges** – Supports both direct queues and exchange-based messaging.
- 🎧 **Event-Driven Model** – Subscribe to packets effortlessly using an event bus.

---

## 📥 Installation

To install **HopperMq**, add the **GitHub Package repository** to your project:

### 🔹 Gradle (Groovy DSL)

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
    implementation "gg.levely.system:hoppermq:0.0.1"
}
```

### 🔸 Gradle (Kotlin DSL)

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
    implementation("gg.levely.system:hoppermq:0.0.1")
}
```

### 🛠 Authentication Setup

Since GitHub Packages requires authentication, you must provide a **GitHub Personal Access Token (PAT)**. You can either:

1. **Set environment variables:**
   ```sh
   export GITHUB_USER=your-username
   export GITHUB_TOKEN=your-personal-access-token
   ```

2. **Or define them in `gradle.properties`:**
   ```properties
   gpr.user=your-username
   gpr.token=your-personal-access-token
   ```

---

## 🎯 Getting Started

### 1️⃣ Initialize HopperMq

```kotlin
val hopperMq = HopperMq("amqp://your-rabbitmq-url", "YourAppName")
```

### 2️⃣ Creating a Packet

Packets are structured messages that can be sent through RabbitMQ.

```kotlin
@RabbitPacketLabel
class ExamplePacket : RabbitPacket {
    
    lateinit var message: String
    
    private constructor() // Required for deserialization
    
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

### 3️⃣ Registering a Packet

Before sending or receiving packets, they must be **registered** in the `PacketRegistry`.

```kotlin
val packetRegistry = hopperMq.getPacketRegistry()

packetRegistry.register("example-packet", ExamplePacket::class.java)
```

> **💡 Automatic Registration:**  
> You can also register all packets within a specific package automatically:
> ```kotlin
> packetRegistry.register("com.yourpackage.packets")
> ```

### 4️⃣ Listening for Packets

To handle incoming packets, subscribe to them via the event bus.

```kotlin
val rabbitBus = hopperMq.getRabbitBus()

rabbitBus.subscribe(ExamplePacket::class.java) { event ->
    println("Received packet with message: ${event.message}")
}
```

### 5️⃣ Creating and Binding Queues

Queues help in managing different communication channels.

```kotlin
val selfQueue = RabbitQueue.of("self-${UUID.randomUUID()}") // Private queue
val commonQueue = RabbitQueue.of("exchange-example-common", "example-common", BuiltinExchangeType.DIRECT) // Shared queue

hopperMq.bindQueue(selfQueue)
hopperMq.bindQueue(commonQueue)
```

### 6️⃣ Sending a Packet

Once the packet is registered, you can send it to a queue.

```kotlin
hopperMq.publish(commonQueue, ExamplePacket("Hello from HopperMq!"))
```

---

## 📌 Advanced Usage

### ➤ Check Connection Status

```kotlin
if (hopperMq.isConnected()) {
    println("HopperMq is connected to RabbitMQ!")
}
```

### ➤ Remove a Queue

```kotlin
hopperMq.removeQueue("queue-name")
```

### ➤ Closing the Connection

Ensure proper resource cleanup:

```kotlin
hopperMq.close()
```

---

## 📜 License

This project is licensed under the **MIT License**.


---

🐰 **Happy Messaging with HopperMq!** 🚀



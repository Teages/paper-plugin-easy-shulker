# Easy Shulker

A lightweight Paper plugin that allows players to open Shulker Boxes directly from their hand without placing them on the ground.

> The project is vibe coding with AI, use at your own risk.

## Features

- **Open from Hand**: Simply right-click while holding a Shulker Box to open it.

## Requirements

- **Server Software**: Paper (or forks)
- **Minecraft Version**: 1.21.8+

## Installation

1. Download the latest release `.jar` file.
2. Place it into your server's `plugins` folder.
3. Restart your server.

## Usage

1. Hold a Shulker Box in your main hand.
2. Right-click in the air (do not sneak).
3. The Shulker Box inventory will open.
4. Close the inventory to save changes.

## Building from Source

This project uses Gradle. To build the plugin locally:

```bash
./gradlew shadowJar
```

The build artifact will be located in `build/libs/`.

## License

This project is licensed under the [MIT License](LICENSE).

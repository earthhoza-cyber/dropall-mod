# Drop All Mod — Fabric 1.21.1

Adds a **"Drop All"** button to every inventory and container screen in Minecraft.  
Pressing it **drops** the items on the ground — it does **not** delete them.

---

## Features
| Screen | Behaviour |
|---|---|
| Player Inventory | Drops slots 9–44 (main inventory + hotbar). Skips armour, offhand, and crafting grid. |
| Chest / Barrel / Shulker Box | Drops only the container's own slots. |
| Furnace / Smoker / Blast Furnace | Drops only the furnace slots (input, fuel, output). |
| Any other `HandledScreen` | Drops the container portion only. |

The button appears at the **bottom-right corner** of each GUI and has a Thai tooltip explaining what it does.

---

## Requirements
- Minecraft **1.21.1**
- Fabric Loader **≥ 0.16.9**  
- Fabric API **0.107.3+1.21.1** (or the `fabric-api-0.141.3+1.21.11.jar` you already have — drop it in `mods/` like usual)

---

## Build

1. Make sure you have **JDK 21** installed.
2. Open a terminal in this folder.
3. Run:
   ```bash
   # Windows
   gradlew.bat build

   # macOS / Linux
   ./gradlew build
   ```
4. The compiled `.jar` will be at:
   ```
   build/libs/dropall-1.0.0.jar
   ```
5. Copy it (along with Fabric API) into your `.minecraft/mods/` folder.

---

## Project structure

```
dropall-mod/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradle/wrapper/
└── src/main/
    ├── java/com/example/dropall/
    │   ├── DropAllMod.java            ← server-side: registers packet handler & drops items
    │   ├── client/
    │   │   └── DropAllClient.java     ← client-side entrypoint
    │   ├── network/
    │   │   └── DropAllPayload.java    ← custom C2S packet
    │   └── mixin/
    │       └── HandledScreenMixin.java ← injects the button into every HandledScreen
    └── resources/
        ├── fabric.mod.json
        └── dropall.mixins.json
```

---

## How it works (technical)

1. `HandledScreenMixin` injects into `HandledScreen.init()` (client-side) and adds a `ButtonWidget`.  
2. When clicked, it sends a `DropAllPayload` packet (`dropall:drop_all`) to the server, carrying a boolean flag indicating whether to drop the container portion only.  
3. On the server, `DropAllMod` receives the packet, iterates the relevant slots of `player.currentScreenHandler`, sets each non-empty slot to `ItemStack.EMPTY`, and calls `player.dropItem(stack, ...)` to physically spawn the item entity.  
4. `handler.sendContentUpdates()` syncs the result back to the client so the UI refreshes immediately.

# Flashback In-Editor Character Animation System — User Guide

This guide covers everything you need to know to create, customize, pose, keyframe, and render animated Minecraft player models in the **Flashback Replay Mod**.

---

## 🚀 1. Quick Start

### Step 1: Open the Characters Window
1. Enter your replay world in Flashback.
2. In the top menu bar, click **Characters** (or press the keybind to toggle the window).
3. The **Characters** panel will appear docked or floating in the editor UI.

### Step 2: Spawn a Character
1. Position your replay camera where you want the character to appear.
2. Click the **`+ Add Character`** button in the Characters window.
3. A player model will appear in front of your camera with a 3D selection box and XYZ coordinate axes.

---

## 🎨 2. Customizing Skins & Models

Navigate to the **`Skin & Model`** tab in the Characters window:

| Setting | Options | How to Use |
| :--- | :--- | :--- |
| **Skin Source** | Default (Steve) / Default (Alex) / Local PNG / Username | Choose where to load the texture from. |
| **Local PNG File** | File Browser | Click **Browse PNG File...** to select any 64x64 or 64x32 `.png` skin from your computer. |
| **Username / UUID** | Online Mojang Fetch | Enter any Minecraft player name (e.g. `Notch`, `Moulberry`) and click **Fetch Skin**. |
| **Model Type** | Auto / Classic (4px) / Slim (3px) | Switch between Steve (wide arms) and Alex (slim arms) geometries. |
| **Outer Layers** | Checkboxes | Toggle Hat, Jacket, Left/Right Sleeves, and Left/Right Pants layers independently. |

---

## 🧭 3. Positioning & Scaling (Transform Tab)

Navigate to the **`Transform`** tab to adjust world coordinates:

- **Position (X, Y, Z)**: Drag the sliders or type exact coordinates.
- **Rotation (Pitch, Yaw, Roll)**: Rotate the character freely in all 3 axes (pitch up/down, yaw left/right, roll tilt).
- **Scale (X, Y, Z)**: Make the character giant, miniature, or adjust proportions.
- **Move to Camera**: Instantly teleports the character to where your replay camera is currently pointing.
- **Look At Character**: Points your camera directly at the character's face.

---

## 🕺 4. Posing & Limb Controls (Pose & Limbs Tab)

### Using Built-in Pose Presets
Select a preset from the **Preset Library** dropdown and click **`Apply Preset`**:
- **Standing** (Default humanoid T-neutral)
- **T-Pose** (Reference pose)
- **Walk Cycle Step A / Step B** (Striding poses)
- **Running** (Full sprint forward-lean pose)
- **Sitting** (Legs 90° forward)
- **Waving** (Right arm raised in greeting)
- **Sneaking** (Crouched forward posture)
- **Pointing** (Arm extended forward)
- **Zombie Arms** (Both arms stretched out)
- **Hands on Hips** (Casual resting pose)

### Fine-Tuning Individual Limbs
Expand any limb header to adjust rotation angles in degrees:
- **Head**: Pitch (look up/down), Yaw (turn left/right), Roll (tilt side-to-side)
- **Torso / Body**: Pitch, Yaw, Roll
- **Left Arm & Right Arm**: Pitch, Yaw, Roll
- **Left Leg & Right Leg**: Pitch, Yaw, Roll

### Saving Custom Poses
1. Pose the character using the sliders.
2. Enter a name in the **Custom Poses** box (e.g. *"Hero Landing"*).
3. Click **`Save Current Pose`**.
4. You can now apply this pose to any character at any tick with one click.

---

## ⏱️ 5. Keyframing & Timeline Animation

You can animate characters to walk through the world, run, turn, jump, and perform gestures over time.

### How to Create an Animation Sequence:

```text
Tick 0:   Character at Point A (Standing Pose) -> [INSERT KEYFRAME]
Tick 40:  Character at Point B (Walking Step A) -> [INSERT KEYFRAME]
Tick 80:  Character at Point C (Walking Step B) -> [INSERT KEYFRAME]
Tick 120: Character at Point D (Waving Pose)    -> [INSERT KEYFRAME]
```

1. **Scrub the Replay Timeline** to the starting tick (e.g. Tick `0`).
2. Move the character to the start position and set their initial pose.
3. In the **`Keyframes`** tab (or **`Transform`** / **`Pose`** tabs), choose your interpolation:
   - **Linear**: Direct constant-speed movement.
   - **Smooth (Catmull-Rom)**: Natural curves and organic acceleration.
   - **Ease In / Ease Out / Ease In-Out**: Smooth deceleration at stops.
   - **Hold / Step**: Instant pose snaps (great for stop-motion or sudden cuts).
4. Click **`INSERT KEYFRAME (ALL CHANNELS)`**.
5. **Scrub forward** on the timeline (e.g. to Tick `60`).
6. Change the character position, rotation, or limb angles.
7. Click **`INSERT KEYFRAME (ALL CHANNELS)`** again.
8. Press **Play (Space)** — the character will smoothly interpolate between keyframes!

---

## 🔄 6. Procedural Animation Clips & Loops

If you don't want to keyframe every leg step manually, use the **`Animation Clip`** tab:

1. Select an **Active Loop**:
   - **Idle Breathing**: Subtle natural chest and arm oscillation.
   - **Walk Cycle**: Automatic leg/arm walking swings.
   - **Run Cycle**: Fast sprint cycle with forward body lean.
   - **Wave Hand**: Continuous waving arm gesture.
   - **Zombie Walk**: Outstretched arms with lumbering steps.
   - **Attack Swing**: Repeated sword/tool swinging motion.
2. Configure:
   - **Loop Animation**: Continuous or single-shot.
   - **Speed Multiplier**: `0.1x` to `3.0x` speed.
   - **Blending Weight**: `0%` to `100%` (blend procedural cycles on top of manual keyframes).

---

## 🎯 7. Viewport Interaction & Multi-Character Management

- **3D Viewport Selection**: Right-click directly on any character model in the 3D replay world to select them.
- **Gizmo Visuals**: The currently selected character displays a cyan bounding box and Red (X), Green (Y), Blue (Z) axis indicators.
- **Multiple Characters**: Add as many independent actors as you need into the scene.
- **Duplicate**: Click **`Duplicate`** to clone a character with all their skin settings, poses, and animation tracks.
- **Undo / Redo**: Standard `Ctrl + Z` / `Ctrl + Y` history supports all character additions, deletions, and keyframe modifications.

---

## 🎬 8. Rendering & Exporting

- When you play back your replay or scrub the timeline, characters update in real time with lighting calculated from the surrounding world blocks and sky.
- When exporting videos via **Export Video (`ExportJob`)**, all animated characters are automatically rendered into the final high-definition video frames with full shader/lighting compatibility.

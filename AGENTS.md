# AGENTS.md

## Scope and precedence
- This file is the project-level guidance for AI coding agents in this repository.
- One glob search for existing AI guidance files returned no matches (`.github/copilot-instructions.md`, `AGENT.md`, `AGENTS.md`, `CLAUDE.md`, `.cursorrules`, `.windsurfrules`, `.clinerules`, `.cursor/rules/**`, `.windsurf/rules/**`, `.clinerules/**`, `README.md`).

## Big picture architecture
- This is a Java 21 modular JavaFX desktop app (`pom.xml`, `module-info.java`), not a web service.
- Entry flow is: `Launcher.main()` -> `Application.launch()` -> `DrawingApplication.start()` -> load `main.fxml`.
- UI composition is declarative in `src/main/resources/com/mazenfahim/drawingboard/main.fxml` and behavior lives in `DrawingController`.
- `SlidesHandler` is the only slide-state manager: it owns `List<Canvas>`, `currentSlideIndex`, and enforces `MAX_SLIDES = 35`.
- Data is in-memory only (per-slide `Canvas` objects). No persistence layer, DB, or file IO in current code paths.

## Component boundaries and data flow
- `DrawingController` orchestrates everything: tool state, keyboard shortcuts, window controls, and canvas event wiring.
- Slide navigation always goes through `SlidesHandler` then `switchToCanvas(...)` in `DrawingController`.
- `switchToCanvas(...)` is the critical seam: it rebinds canvas size to `mainStackPane`, resets drawing/eraser state, and reattaches handlers.
- Erasing is pixel-based (`eraseCircle`, `eraseLine`) via `PixelWriter#setArgb(..., 0x00000000)` for transparent pixels.
- Top window bar behavior is custom (undecorated stage + animated reveal/hide), implemented in `setupWindowBar()` + root mouse handlers from FXML.

## Developer workflows
- Prereq: Java 21 and `JAVA_HOME` must be set; Maven wrapper is committed (`mvnw`, `mvnw.cmd`).
- Compile: `./mvnw.cmd -DskipTests compile`
- Run app: `./mvnw.cmd javafx:run`
- Tests (if/when added): `./mvnw.cmd test`
- Packaging hints are in `javafx-maven-plugin` config (`launcher`/`jlinkImageName` = `EasyBrush`).
- Note: local verification in this environment failed until `JAVA_HOME` is configured.

## Project-specific conventions to preserve
- Keep controller/FXML fx:id alignment strict; changing `fx:id` or handler names requires synchronized updates in both files.
- Tool button active state is CSS-driven (`tool-btn-active`) and toggled in code (`toggleEraser`, color change listener, `switchToCanvas`).
- Slide label and button enabled state are refreshed through `updateCurrentSlide()` after every slide mutation/navigation.
- Canvas is expected to fill `mainStackPane`; new features should keep width/height binding behavior intact.
- Keyboard shortcuts are centralized in the scene key filter (`RIGHT/LEFT/C/E/DELETE/D/R`) inside `initialize()`.

## External dependencies and integration points
- JavaFX modules in use: `javafx-controls`, `javafx-fxml`.
- Iconography uses Ikonli (`org.kordamp.ikonli`), with icon literals directly in FXML and icon-state updates in controller code.
- `pdfbox` exists in `pom.xml` but is not currently referenced in `src/main/java`; treat as dependency-present, feature-not-integrated.
- Resources are classpath-loaded from `src/main/resources/com/mazenfahim/drawingboard/` (FXML, CSS, icons).

## Safe change strategy for agents
- For behavior changes, start at `DrawingController` then confirm corresponding FXML/CSS contracts.
- For slide-related work, modify `SlidesHandler` and the post-action `updateCurrentSlide()` call sites together.
- For UI polish, prefer CSS adjustments in `main.css` before adding Java logic.
- Avoid introducing persistence/export assumptions unless explicitly requested; current app model is ephemeral drawing sessions.


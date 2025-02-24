# Contributing to Mnemosyne

Contributions are welcome.

## Getting Started

### 1. Fork the Repository
- Click the **Fork** button on the repository’s GitHub page.
- Clone your fork:
  ```sh
  git clone https://github.com/your-username/[project-name].git
  cd [project-name]
  ```

### 2. Set Up the Development Environment
- Ensure you have the required dependencies installed.
- Follow the setup guide in `README.md` to configure your environment.
- Install dependencies:
  ```sh
  [installation command]
  ```

### 3. Create a Feature Branch
- Always work on a new branch, never on `main` or `master`.
- Use a descriptive branch name:
  ```sh
  git checkout -b feature-xyz
  ```

## Making Changes

### Code Style
- Follow the project's coding style and best practices.
- Ensure the code is clean, well-commented, and adheres to the existing structure.
- Run the formatter/linter before committing:
  ```sh
  [linter command]
  ```

### Commit Messages
- Write meaningful commit messages following this format:
  ```
  [type]: [short description]
  
  [Longer explanation if necessary]
  ```
- Example:
  ```
  fix: resolve issue with login timeout
  
  Adjusted session handling to prevent premature logout due to token expiration.
  ```

### Running Tests
- Before submitting changes, ensure all tests pass:
  ```sh
  [test command]
  ```
- If you add a new feature, write relevant tests.

## Submitting Changes

1. **Push your branch to your fork**:
   ```sh
   git push origin feature-xyz
   ```
2. **Open a Pull Request (PR)**:
   - Go to the main repository and click **New Pull Request**.
   - Provide a clear title and description of the changes.
   - Link related issues if applicable (`Closes #issue-number`).

## Reporting Issues

If you find a bug or have a feature request:
- Check **existing issues** before opening a new one.
- Provide **steps to reproduce** the issue.
- Include relevant logs, screenshots, or error messages if applicable.
- Suggest possible solutions if you have ideas.

## Code of Conduct
By participating in this project, you agree to abide by the [Code of Conduct](CODE_OF_CONDUCT.md).

If you have any questions, feel free to open a discussion.



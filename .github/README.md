# GitHub Configuration

This directory contains GitHub-specific configuration files for the Room Scanner project.

## Files in This Directory

### `copilot-instructions.md`

Comprehensive instructions and guidelines for GitHub Copilot coding agent when working on this repository.

**Key sections include:**
- How to use Copilot coding agent with this repository
- Appropriate tasks for Copilot and what to avoid
- Project overview and technology stack
- Build and testing commands
- Code conventions and architecture patterns
- Common development tasks
- Security considerations and best practices

**For contributors:** If you're using GitHub Copilot coding agent, please read the [copilot-instructions.md](copilot-instructions.md) file first. It provides important context about the project structure, conventions, and workflows.

**For maintainers:** Keep the instructions up-to-date as the project evolves. Update them when:
- Adding new major features or components
- Changing build or test processes
- Modifying architecture patterns
- Adding new dependencies or tools
- Changing contribution workflows

## Using GitHub Copilot Coding Agent

### Quick Start

1. **Assign an issue to @copilot** on GitHub
2. Copilot will analyze the repository using the instructions in `copilot-instructions.md`
3. It will create a pull request with proposed changes
4. Review the PR and provide feedback by mentioning @copilot
5. Approve and merge once satisfied

### Best Practices

- **Write clear issue descriptions**: Include context, acceptance criteria, and examples
- **Review carefully**: Always review Copilot's changes before merging
- **Iterate as needed**: Provide feedback and request changes through PR comments
- **Verify tests pass**: Ensure all CI checks pass before merging
- **Check documentation**: Make sure docs are updated for significant changes

### Resources

- [GitHub Copilot Documentation](https://docs.github.com/en/copilot)
- [Best practices for Copilot coding agent](https://docs.github.com/en/copilot/tutorials/coding-agent/get-the-best-results)
- [Copilot coding agent tutorials](https://docs.github.com/en/copilot/tutorials/coding-agent)

## Future Enhancements

This directory can be expanded with:

- **Custom Agents** (`.github/agents/`): Specialized agents for specific tasks
- **Issue Templates** (`.github/ISSUE_TEMPLATE/`): Structured issue creation
- **Pull Request Template** (`.github/PULL_REQUEST_TEMPLATE.md`): PR guidelines
- **Workflows** (`.github/workflows/`): CI/CD automation with GitHub Actions

## Maintenance

The configuration files in this directory should be reviewed and updated regularly to ensure they remain accurate and helpful. If you notice outdated information or have suggestions for improvement, please open an issue or submit a pull request.

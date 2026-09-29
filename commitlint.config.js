export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'docs', 'refactor', 'chore', 'rename', 'style', 'remove'],
    ],
    'scope-empty': [2, 'always'],
    'subject-case': [0],
    'subject-full-stop': [0],
  },
};

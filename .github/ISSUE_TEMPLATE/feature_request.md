name: Feature request
description: Suggest an idea for CraftBook
labels: ['type:feature-request', 'status:pending']

body:
- type: markdown
  attributes:
  value: >
  Please note: we are currently not accepting feature requests for CraftBook 3. All future development is going towards CraftBook 5.

- type: textarea
  attributes:
  label: The Problem
  description: >
  What is making your CraftBook experience sub-optimal? This should be something that
  cannot be easily solved by existing CraftBook features.
  placeholder: It's hard to ... ; I'm unable to ...
  validations:
  required: true

- type: textarea
  attributes:
  label: A Solution
  description: What is your proposed solution to the above problem?
  validations:
  required: true

- type: textarea
  attributes:
  label: Alternatives
  description: |
  Alternative solutions or workarounds to the problem.
  You should also describe why these are not preferable to the given solution.
  validations:
  required: false

- type: textarea
  attributes:
  label: Anything Else?
  description: Add any additional context you can provide below.
module.exports = {
  docs: [
    {
      type: 'category',
      label: 'Overview',
      items: ['index', 'ai-project-context', 'project-overview'],
    },
    {
      type: 'category',
      label: 'Guides',
      items: ['copilot-instructions', 'cursor-rules'],
    },
    {
      type: 'category',
      label: 'Components',
      items: [
        'components',
        {
          type: 'category',
          label: 'Component Details',
          items: [
            'components/cache',
            'components/dfs',
            'components/database-architecture',
            'components/extension',
            'components/scheduler',
            'components/tracer',
            'components/testcontainers',
          ],
        },
      ],
    },
    {
      type: 'category',
      label: 'Commons',
      items: [
        'commons',
        {
          type: 'category',
          label: 'Commons Details',
          items: ['commons/commons-dto', 'commons/commons-util'],
        },
      ],
    },
    {
      type: 'category',
      label: 'Modules',
      items: ['modules', 'modules/upms', 'modules/upms-architecture'],
    },
    'gateway',
    'dependencies',
    'application',
  ],
};

// Docusaurus configuration for LoadUp documentation site
module.exports = {
  title: 'LoadUp Framework',
  tagline: 'Enterprise-grade application platform',
  url: 'https://your-domain.com',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'loadup-cloud',
  projectName: 'loadup-framework',
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: 'https://github.com/loadup-cloud/loadup-framework/tree/main/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
  themeConfig: {
    navbar: {
      title: 'LoadUp',
      logo: {
        alt: 'LoadUp Logo',
        src: 'img/logo.svg',
      },
      items: [
        {to: 'docs/', label: 'Docs', position: 'left'},
        {href: 'https://github.com/loadup-cloud/loadup-framework', label: 'GitHub', position: 'right'},
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {label: 'Overview', to: 'docs/'},
            {label: 'Components', to: 'docs/components'},
          ],
        },
        {
          title: 'Community',
          items: [
            {label: 'GitHub', href: 'https://github.com/loadup-cloud/loadup-framework'},
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} LoadUp`,
    },
  },
};

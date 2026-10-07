// eslint.config.js
import js from "@eslint/js";
import typescriptEslint from "@typescript-eslint/eslint-plugin";
import typescriptParser from "@typescript-eslint/parser";
import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";
import prettier from "eslint-plugin-prettier";
import prettierConfig from "eslint-config-prettier";

export default [
  // Base configuration
  js.configs.recommended,

  // Global settings
  {
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
      globals: {
        // Browser globals
        window: "readonly",
        document: "readonly",
        console: "readonly",
        fetch: "readonly",
        location: "readonly",
        history: "readonly",
        navigator: "readonly",
        setTimeout: "readonly",
        clearTimeout: "readonly",
        setInterval: "readonly",
        clearInterval: "readonly",
        requestAnimationFrame: "readonly",
        cancelAnimationFrame: "readonly",
        sessionStorage: "readonly",
        localStorage: "readonly",
        URL: "readonly",
        URLSearchParams: "readonly",
        FormData: "readonly",
        Headers: "readonly",
        Request: "readonly",
        Response: "readonly",
        AbortController: "readonly",
        CustomEvent: "readonly",
        TextEncoder: "readonly",
        TextDecoder: "readonly",
        TransformStream: "readonly",
        MutationObserver: "readonly",
        IntersectionObserver: "readonly",
        NodeJS: "readonly",
        React: "readonly",
        import: "readonly",
        HTMLSelectElement: "readonly",
        HTMLInputElement: "readonly",
        EventListener: "readonly",
      },
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
        },
      },
    },
    settings: {
      react: {
        version: "detect",
      },
    },
    ignores: [
      "node_modules/**",
      "build/**",
      "dist/**",
      ".server/**",
      ".client/**",
      ".react-router/**",
      "cypress/downloads/**",
      "cypress/screenshots/**",
      "**/*.min.js",
      "**/*.bundle.js",
      "**/build/**",
      "**/dist/**",
    ],
  },

  // TypeScript files
  {
    files: ["**/*.ts", "**/*.tsx"],
    languageOptions: {
      parser: typescriptParser,
    },
    plugins: {
      "@typescript-eslint": typescriptEslint,
      react: react,
      "react-hooks": reactHooks,
      prettier: prettier,
    },
    rules: {
      ...prettierConfig.rules,
      // TypeScript rules
      "@typescript-eslint/no-unused-vars": ["warn", { argsIgnorePattern: "^_" }],
      "@typescript-eslint/no-explicit-any": "warn",
      "@typescript-eslint/no-non-null-assertion": "warn",

      // React rules
      "react/react-in-jsx-scope": "off", // Not needed with React 17+
      "react/prop-types": "off", // Using TypeScript for prop validation
      "react/jsx-uses-react": "off",
      "react/jsx-uses-vars": "error",
      "react/jsx-key": "error",
      "react/jsx-no-duplicate-props": "error",
      "react/jsx-no-undef": "error",
      "react/no-children-prop": "error",
      "react/no-danger-with-children": "error",
      "react/no-deprecated": "error",
      "react/no-direct-mutation-state": "error",
      "react/no-find-dom-node": "error",
      "react/no-is-mounted": "error",
      "react/no-render-return-value": "error",
      "react/no-string-refs": "error",
      "react/no-unescaped-entities": "error",
      "react/no-unknown-property": "error",
      "react/no-unsafe": "error",
      "react/require-render-return": "error",

      // React Hooks rules
      "react-hooks/rules-of-hooks": "error",
      "react-hooks/exhaustive-deps": "warn",

      // General rules
      "no-console": "warn",
      "no-debugger": "error",
      "no-duplicate-imports": "error",
      "no-unused-vars": "off", // Using @typescript-eslint/no-unused-vars instead
      "prefer-const": "error",
      "no-var": "error",
      "no-empty-pattern": "error",

      // Prettier rules
      "prettier/prettier": "warn",
    },
  },

  // JavaScript files
  {
    files: ["**/*.js", "**/*.jsx"],
    plugins: {
      react: react,
      "react-hooks": reactHooks,
    },
    rules: {
      "react/react-in-jsx-scope": "off",
      "react/prop-types": "warn",
      "react-hooks/rules-of-hooks": "error",
      "react-hooks/exhaustive-deps": "warn",
    },
  },

  // Cypress files
  {
    files: ["cypress/**/*.ts", "cypress/**/*.js"],
    languageOptions: {
      globals: {
        cy: "readonly",
        Cypress: "readonly",
        describe: "readonly",
        it: "readonly",
        beforeEach: "readonly",
        afterEach: "readonly",
        before: "readonly",
        after: "readonly",
        expect: "readonly",
        context: "readonly",
        specify: "readonly",
        xdescribe: "readonly",
        xit: "readonly",
        xcontext: "readonly",
        xspecify: "readonly",
        skip: "readonly",
        only: "readonly",
        JQuery: "readonly",
        HTMLElement: "readonly",
      },
    },
    rules: {
      "no-console": "off", // Allow console in tests
      "no-undef": "off", // Cypress globals are handled above
      "@typescript-eslint/no-unused-vars": ["warn", { argsIgnorePattern: "^_" }],
    },
  },

  // Node.js files
  {
    files: ["*.config.js", "*.config.ts", ".eslintrc.cjs", "cypress/mocks/**/*.ts"],
    languageOptions: {
      globals: {
        process: "readonly",
        __dirname: "readonly",
        __filename: "readonly",
        module: "readonly",
        require: "readonly",
        exports: "readonly",
        console: "readonly",
      },
    },
    rules: {
      "@typescript-eslint/no-var-requires": "off",
      "no-console": "off", // Allow console in config files
      "no-undef": "off", // Node globals are handled above
    },
  },

  // Ignore build files completely
  {
    ignores: [
      "build/**",
      "dist/**",
      ".react-router/**",
      "**/build/**",
      "**/dist/**",
      "**/*.min.js",
      "**/*.bundle.js",
    ],
  },
];

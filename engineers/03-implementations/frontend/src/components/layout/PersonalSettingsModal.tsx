import React from 'react';
import { Renderer } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';

const modalSpec = {
  "root": "onboarding-modal",
  "elements": {
    "onboarding-modal": {
      "type": "Dialog",
      "props": {
        "id": "onboarding-modal",
        "label": "Personal Settings",
        "title": "⚙️ Personal Settings",
        "description": "Please set your learning goals below.",
        "openPath": "/modals/onboarding-modal"
      },
      "children": [
        "welcome-message",
        "target-level-select",
        "daily-target-questions-select",
        "start-onboarding-btn"
      ],
      "visible": {
        "$state": "/modals/onboarding-modal"
      }
    },
    "welcome-message": {
      "type": "div",
      "props": {
        "className": "text-lg mb-4 text-center font-medium flex justify-center gap-1"
      },
      "visible": {
        "$state": "/data/isFirstTimeOnboarding"
      },
      "children": [
        "welcome-text1",
        "welcome-text2",
        "welcome-text3"
      ]
    },
    "welcome-text1": {
      "type": "Text",
      "props": {
        "text": "Hello, ",
        "className": "inline"
      },
      "children": []
    },
    "welcome-text2": {
      "type": "Text",
      "props": {
        "text": {
          "$state": "/data/user/email"
        },
        "className": "inline text-primary"
      },
      "children": []
    },
    "welcome-text3": {
      "type": "Text",
      "props": {
        "text": "!",
        "className": "inline"
      },
      "children": []
    },
    "target-level-select": {
      "type": "Select",
      "props": {
        "id": "target-level-select",
        "name": "targetLevel",
        "label": "Target Level",
        "required": true,
        "placeholder": "Select Target Level",
        "options": [
          "國中2000單字",
          "高中7000單字"
        ],
        "checks": null,
        "validateOn": null,
        "value": {
          "$bindState": "/form/target-level-select"
        }
      },
      "children": []
    },
    "daily-target-questions-select": {
      "type": "Select",
      "props": {
        "id": "daily-target-questions-select",
        "name": "dailyTargetQuestions",
        "label": "Daily Target Questions",
        "required": true,
        "placeholder": "Select Daily Target Questions",
        "options": [
          "10",
          "20",
          "50",
          "100"
        ],
        "checks": null,
        "validateOn": null,
        "value": {
          "$bindState": "/form/daily-target-questions-select"
        }
      },
      "children": []
    },
    "start-onboarding-btn": {
      "type": "Button",
      "props": {
        "id": "start-onboarding-btn",
        "label": "Save",
        "variant": "primary",
        "disabled": null
      },
      "children": [],
      "on": {
        "press": [
          {
            "action": "executeBehavior",
            "params": {
              "ref": "onboardUser",
              "payload": {
                "targetLevel": {
                  "$state": "/form/target-level-select"
                },
                "dailyTargetQuestions": {
                  "$state": "/form/daily-target-questions-select"
                }
              }
            }
          }
        ]
      }
    }
  }
};

export default function PersonalSettingsModal() {
  return <Renderer spec={modalSpec as any} registry={componentRegistry} />;
}

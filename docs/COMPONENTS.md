# Layer Components

Recipes this layer provides today. Add a row when a new component recipe
lands; the layer tree is the source of truth for what actually ships.

| Component | Upstream | Purpose |
|-----------|----------|---------|
| [`aktualizr-lite`](../recipes-sota/aktualizr-lite/aktualizr-lite_97.bb) | [`foundriesio/aktualizr-lite`](https://github.com/foundriesio/aktualizr-lite) | Foundries.io™ OTA+ update client |
| [`composectl`](../recipes-containers/composeapp/composectl_97.bb) | [`foundriesio/composeapp`](https://github.com/foundriesio/composeapp) | CLI utility to manage compose apps |
| [`fio-device-register`](../recipes-sota/fio-device-register/fio-device-register_97.bb) | [`foundriesio/lmp-device-register`](https://github.com/foundriesio/lmp-device-register) | Device registration tool for Foundries.io™ OTA+ |
| [`fio-diag`](../recipes-support/fio-diag/fio-diag_1.2.bb) | [`foundriesio/lmp-tools`](https://github.com/foundriesio/lmp-tools/tree/master/device-scripts) | Diagnostic script the fioconfig `diag` remote action runs |
| [`fioconfig`](../recipes-support/fioconfig/fioconfig_97.bb) | [`foundriesio/fioconfig`](https://github.com/foundriesio/fioconfig) | Device configuration daemon and handlers; remote actions behind the `actions` PACKAGECONFIG, VPN support behind `vpn` |
| [`packagegroup-foundries-containers`](../recipes-containers/packagegroups/packagegroup-foundries-containers.bb) | — | Installs the container runtime group and the compose-app manager named by `COMPOSE_APP_MANAGER` |
| [`packagegroup-foundries-docker`](../dynamic-layers/virtualization-layer/recipes-containers/packagegroups/packagegroup-foundries-docker.bb) | — | Installs the Docker engine and the Compose plugin; needs the meta-virtualization layer and `virtualization` in `DISTRO_FEATURES` |
| [`packagegroup-foundries-platform`](../recipes-sota/packagegroups/packagegroup-foundries-platform.bb) | — | Installs the OTA client named by `SOTA_CLIENT`, the registration tool, and the configuration daemon |

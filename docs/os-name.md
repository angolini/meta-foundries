# OS Name

The OS name is the name of the ostree deployment, also called the stateroot. A device holds its deployments under `/ostree/deploy/<os-name>/`.

## Where It Comes From

meta-updater defines the name as `OSTREE_OSNAME` and defaults it to `nodistro`. The image creates its deployment under that name.

## Set It

Set `OSTREE_OSNAME` in `conf/local.conf`:

```
OSTREE_OSNAME = "myos"
```

Or in a kas file:

```yaml
local_conf_header:
  osname: |
    OSTREE_OSNAME = "myos"
```

Use `=`, not `?=`, so the value wins over the meta-updater default.

## Choose It Before the Fleet Exists

The name is written to the device once, when it is flashed. OTA updates never rename `/ostree/deploy/<os-name>/`.

Changing `OSTREE_OSNAME` affects only devices flashed with the new image. Devices already in the field keep their name. They still take updates built with the new one, because aktualizr-lite derives the name from the deployment the device booted.

## Do Not Set `pacman.os`

aktualizr-lite accepts a `[pacman] os` setting that overrides the derived name. Leave it unset. If a device receives a value that differs from its deployment name, every later update fails with `No merge deployment`. The device stays on the image it runs.

## Check It on a Device

```sh
ls /ostree/deploy/
ostree admin status
```

`ostree admin status` prints the name before each deployment:

```
* myos 8a2b46bc....0
  myos 454da8df....0 (rollback)
```

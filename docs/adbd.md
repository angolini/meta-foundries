# How to Enable adb When You Do Not Have a Bughopper

On the Arduino UNO Q, a Bughopper gives you the serial console. Without one, adb over the USB-C port gives you a shell on the board.

meta-foundries does not install adbd. Whether an image ships adbd is a distro decision, so you enable it in your own layer or build configuration.

## What You Need

- meta-qcom, which provides the `image-adbd` class.
- meta-oe from OpenEmbedded, which provides adbd in the `android-tools` recipe.

## Enable adbd at Build Time

Add to `conf/local.conf`:

```
IMAGE_CLASSES += "image-adbd"
EXTRA_IMAGE_FEATURES += "enable-adbd"
IMAGE_INSTALL:append = " kernel-module-libcomposite kernel-module-usb-f-fs"
```

Or in a kas file:

```yaml
local_conf_header:
  adbd: |
    IMAGE_CLASSES += "image-adbd"
    EXTRA_IMAGE_FEATURES += "enable-adbd"
    IMAGE_INSTALL:append = " kernel-module-libcomposite kernel-module-usb-f-fs"
```

Each line does one thing:

- `image-adbd` installs adbd.
- `enable-adbd` starts adbd at boot.
- The kernel modules provide the USB gadget support that adbd needs.

`IMAGE_CLASSES` applies to every image in the build, the initramfs included. To add adbd to one image only, put this in that image recipe instead of the first two lines:

```
inherit image-adbd
IMAGE_FEATURES += "enable-adbd"
```

The modules are still needed in that case.

## The USB Gadget Modules

The UNO Q kernel builds `libcomposite` and `usb_f_fs` as modules. Without them, adbd fails to start.

meta-oe e37c3ee6 makes adbd recommend both modules. With a meta-oe that contains it, drop the `IMAGE_INSTALL` line. meta-qcom locks such a meta-oe since 83dcb6cd.

## Start adbd on a Running Device

Without `enable-adbd`, adbd is installed but does not start. To start it from a shell on the device, over SSH for example:

```sh
touch /etc/usb-debugging-enabled
systemctl start android-tools-adbd
```

The file stays, so adbd also starts on the next boot. Remove it to stop that.

## What meta-qcom-distro Images Already Do

`qcom-minimal-image`, and the images built on it, already inherit `image-adbd` and install `kernel-modules`. They leave `enable-adbd` off, so adbd does not start until you add the feature or start it by hand.

## Let Your Computer Reach the Board

On Linux®, your user needs access to the board's USB device. Find the vendor and product IDs of your board:

```sh
lsusb | grep -i arduino
```

The output looks like this:

```
Bus 003 Device 012: ID 2341:abcd Arduino SA Uno Q
```

Not every UNO Q reports the same product ID, so use the IDs from your own board. This rule, for example, matches vendor `2341` and product `abcd`:

```sh
echo 'SUBSYSTEM=="usb", ATTR{idVendor}=="2341", ATTR{idProduct}=="abcd", MODE="0660", TAG+="uaccess"' \
  | sudo tee /etc/udev/rules.d/99-uno-q.rules
sudo udevadm control --reload-rules
sudo udevadm trigger
```

Unplug the board and plug it back in, because the rule only applies to devices that appear after it exists.

## Check It

Connect the UNO Q USB-C port to your computer and run:

```sh
adb devices
adb shell
```

If `adb devices` lists nothing, restart the adb server with `adb kill-server`. The server keeps the permissions it had when it started.

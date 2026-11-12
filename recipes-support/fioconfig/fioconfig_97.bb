SUMMARY = "Foundries.io OTA+ device configuration daemon"
DESCRIPTION = "A daemon to handle configuration management for devices in a \
Foundries Factory"
HOMEPAGE = "https://github.com/foundriesio/fioconfig"
SECTION = "devel"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://src/${GO_IMPORT}/LICENSE;md5=504a5c2455c8bb2fc5b7667833ab1a68"

GO_IMPORT = "github.com/foundriesio/fioconfig"
GO_IMPORT_PROTO ?= "https"

PV = "97+git"
BRANCH ?= "main"
SRCREV = "2f641ccdc9202ae776137cfaa86de761f7f7af55"

SRC_URI = "\
    git://${GO_IMPORT};protocol=${GO_IMPORT_PROTO};branch=${BRANCH};destsuffix=${GO_SRCURI_DESTSUFFIX} \
    file://fioconfig.service \
    file://fioconfig.path \
    file://fioconfig-extract.service \
"

UPSTREAM_CHECK_COMMITS = "1"

inherit go-mod go-mod-update-modules systemd

require ${BPN}-go-mods.inc
require ${BPN}-licenses.inc

PACKAGECONFIG ?= "actions pkcs11"
PACKAGECONFIG[vpn] = ",,,networkmanager-nmcli wireguard-tools"
PACKAGECONFIG[actions] = ",,,fio-diag"
PACKAGECONFIG[pkcs11] = ",,,"

FIOCONFIG_GO_TAGS = "\
    ${@bb.utils.contains('PACKAGECONFIG', 'vpn', 'vpn', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'actions', '', 'disable_remoteactions', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'pkcs11', '', 'disable_pkcs11', d)} \
"

GOBUILDFLAGS += "${@'-tags ' + ','.join(d.getVar('FIOCONFIG_GO_TAGS').split()) if d.getVar('FIOCONFIG_GO_TAGS').split() else ''}"
GO_EXTRA_LDFLAGS = "-X ${GO_IMPORT}/internal.Commit=${SRCREV}"

SYSTEMD_SERVICE:${PN} = "fioconfig.service fioconfig-extract.service fioconfig.path"

FIOCONFIG_HANDLERS = "aktualizr-toml-update renew-client-cert"
FIOCONFIG_HANDLERS += "${@bb.utils.contains('PACKAGECONFIG', 'vpn', 'factory-config-vpn', '', d)}"
FIOCONFIG_HANDLERS += "${@bb.utils.contains('PACKAGECONFIG', 'actions', 'fioconfig-oneshot', '', d)}"
FIOCONFIG_ACTIONS = "${@bb.utils.contains('PACKAGECONFIG', 'actions', 'diag reboot', '', d)}"

do_install:append() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/fioconfig.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${UNPACKDIR}/fioconfig.path ${D}${systemd_system_unitdir}/
    install -m 0644 ${UNPACKDIR}/fioconfig-extract.service ${D}${systemd_system_unitdir}/

    install -d ${D}${datadir}/fioconfig/handlers
    for handler in ${FIOCONFIG_HANDLERS}; do
        install -m 0755 ${S}/src/${GO_IMPORT}/contrib/${handler} ${D}${datadir}/fioconfig/handlers
    done

    if [ -n "${FIOCONFIG_ACTIONS}" ]; then
        install -d ${D}${datadir}/fioconfig/actions
        for action in ${FIOCONFIG_ACTIONS}; do
            install -m 0755 ${S}/src/${GO_IMPORT}/contrib/actions/${action} ${D}${datadir}/fioconfig/actions
        done
    fi

    if [ -n "${SOTA_CLIENT}" ]; then
        sed -i -e "s/SOTA_CLIENT=aktualizr-lite/SOTA_CLIENT=${SOTA_CLIENT}/g" \
            ${D}${datadir}/fioconfig/handlers/aktualizr-toml-update
    fi
}

FILES:${PN} += "\
    ${systemd_system_unitdir}/fioconfig.service \
    ${systemd_system_unitdir}/fioconfig.path \
    ${systemd_system_unitdir}/fioconfig-extract.service \
"

RDEPENDS:${PN} += "${@d.getVar('SOTA_CLIENT') or ''}"

# The append changes dockerd only in a distro that sets up OTA updates with
# containers, which keeps the docker-moby signature intact elsewhere.
require ${@bb.utils.contains('DISTRO_FEATURES', 'sota virtualization', 'docker-moby-foundries.inc', '', d)}

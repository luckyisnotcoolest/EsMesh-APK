import os
import sys

Import("env")

def merge_binaries(source, target, env):
    """
    Automatically creates a single unified 'esmesh_merged_firmware.bin' at 0x00000 offset
    containing bootloader, partition table, boot app0, and application firmware binary.
    """
    build_dir = env.subst("$BUILD_DIR")
    prog_name = env.subst("$PROGNAME")
    chip_name = env.get("BOARD_MCU", "esp32")

    bootloader_bin = os.path.join(build_dir, "bootloader.bin")
    partitions_bin = os.path.join(build_dir, "partitions.bin")
    firmware_bin = os.path.join(build_dir, f"{prog_name}.bin")
    merged_output_bin = os.path.join(build_dir, f"esmesh_{chip_name}_merged.bin")
    root_dist_bin = os.path.join(env.subst("$PROJECT_DIR"), "dist", f"esmesh_{chip_name}_merged.bin")

    # Ensure dist folder exists
    os.makedirs(os.path.dirname(root_dist_bin), exist_ok=True)

    # Offsets vary slightly between classic ESP32 (0x1000) and ESP32-S3/C3/C6 (0x0000)
    bootloader_offset = "0x0" if chip_name in ["esp32s3", "esp32c3", "esp32c6"] else "0x1000"
    partitions_offset = "0x8000"
    boot_app0_offset = "0xe000"
    app_offset = "0x10000"

    print(f"\n[EsMesh Merge] Packaging single unified flash binary for {chip_name}...")

    # esptool command to merge binaries into a single image starting at 0x0
    merge_cmd = (
        f'"{sys.executable}" -m esptool --chip {chip_name} merge_bin '
        f'-o "{merged_output_bin}" '
        f"--fill-flash-size 4MB "
        f"{bootloader_offset} \"{bootloader_bin}\" "
        f"{partitions_offset} \"{partitions_bin}\" "
        f'{app_offset} "{firmware_bin}"'
    )

    result = env.Execute(merge_cmd)
    if result == 0 and os.path.exists(merged_output_bin):
        # Also copy to project dist folder
        import shutil
        shutil.copy2(merged_output_bin, root_dist_bin)
        print(f"[EsMesh Merge SUCCESS] Unified merged binary ready at:")
        print(f" -> {merged_output_bin}")
        print(f" -> {root_dist_bin}\n")

# Run merge action immediately after firmware.bin is generated
env.AddPostAction("$BUILD_DIR/${PROGNAME}.bin", merge_binaries)

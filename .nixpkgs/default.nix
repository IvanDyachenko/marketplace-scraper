self: super: {
  kafkacat = super.callPackage (import ./overlays/kafkacat.nix) {};
}

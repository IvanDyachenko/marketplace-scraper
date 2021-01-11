let
  pkgs = import <nixpkgs> {
    overlays =
      (import <nixpkgs> {}).overlays ++
      [
        (import ./.nixpkgs/default.nix)
      ];
  };
in
  pkgs.mkShell {
    buildInputs =
      [
        pkgs.kafkacat
        pkgs.metals-emacs
      ];
  }


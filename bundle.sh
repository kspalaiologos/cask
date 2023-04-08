mkdir tmp && cd tmp
unzip "../$1"
7z a -t7z -m0=lzma -mx=9 -mfb=64 -md=32m -ms=on "../${1%.jar}.cask" *
cd .. && rm -rf tmp
jar uf "$2" "MANIFEST" "${1%.jar}.cask"
rm "${1%.jar}.cask"
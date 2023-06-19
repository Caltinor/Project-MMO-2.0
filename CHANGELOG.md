# Changelog

## [1.20.1-1.0.1] - 2023-06-19
- Added `modFilter` option to `genData` to generate files for specific mods
  - supports multiple calls to add multiple mods to the filter
  - note: player filters always had this repeat calling behavior
- Fixed excessive dig speed
- Fixed vein "correct tool" check being arbitrarily restrictive
- Fixed crash from childless breeding
- Fixed loss on death = 0.0 causing maximum losses instead of none

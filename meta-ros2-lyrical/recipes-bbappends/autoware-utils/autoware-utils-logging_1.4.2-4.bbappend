# Copyright (c) 2026 Wind River Systems, Inc.

# error: 'template<class _Codecvt, class _Elem, class _WideAlloc, class
# _ByteAlloc> class std::wstring_convert' is deprecated [-Werror=deprecated-declarations]
# std::wstring_convert is deprecated since C++17; this project's -Werror
# escalates the deprecation warning to a hard error against GCC 15.
CXXFLAGS += "-Wno-error=deprecated-declarations"

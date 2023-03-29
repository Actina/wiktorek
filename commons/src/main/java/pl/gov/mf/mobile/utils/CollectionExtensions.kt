package pl.gov.mf.mobile.utils

fun <T> Collection<T>.containsOnly(value: T): Boolean {
    return size == 1 && contains(value)
}
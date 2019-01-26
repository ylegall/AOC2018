package util

fun <T> Iterable<T>.permutations(): List<List<T>> {
    return permutations(listOf(), this.toSet())
}

private fun <T> permutations(current: List<T>, options: Set<T>): List<List<T>> {
    return if (options.isEmpty()) {
        listOf(current)
    } else {
        options.flatMap { item ->
            permutations(current + item, options.minusElement(item))
        }
    }
}
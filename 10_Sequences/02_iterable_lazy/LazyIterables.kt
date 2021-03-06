
fun <T> Iterable<T>.inspect(inspector: (T) -> Unit) : Iterable<T> {
	return object : Iterable<T> {
		override fun iterator() : Iterator<T> {
			val source = this@inspect.iterator()
			return object : Iterator<T> {
				override fun hasNext() : Boolean = source.hasNext()
				override fun next() : T {
					val item = source.next()
					inspector(item)
					return item
				}
			}
		}
	}
}

private enum class WhereIterableState {
	UNKNOWN, ITEM_FOUND, NO_MORE_ITEMS
}

fun <T> Iterable<T>.where(predicate : (T) -> Boolean) : Iterable<T> {
	return object : Iterable<T> {
		override fun iterator() : Iterator<T> {
			val source = this@where.iterator()
			return object : Iterator<T> {
				private var state = WhereIterableState.UNKNOWN
				private var curr : T? = null
				
				override fun hasNext() : Boolean =
					updatedState() == WhereIterableState.ITEM_FOUND
				
				override fun next() : T {
					if (updatedState() == WhereIterableState.NO_MORE_ITEMS) {
						throw Exception("No more items")
					}
					val res = curr

					curr = null
					state = WhereIterableState.UNKNOWN

					return res ?: throw Exception("Invalid state")
				}
				
				private fun updatedState() : WhereIterableState {
					if (state == WhereIterableState.UNKNOWN) {
						state = WhereIterableState.NO_MORE_ITEMS
						while (source.hasNext()) {
							val item = source.next()
							if (predicate(item)) {
								curr = item
								state = WhereIterableState.ITEM_FOUND
								break
							}
						}
					}
					return state
				}
			}
		}
	}
}

fun <T,R> Iterable<T>.select(mapper: (T) -> R) : Iterable<R> {
	return object : Iterable<R> {
		override fun iterator() : Iterator<R> {
			val source = this@select.iterator()
			return object : Iterator<R> {
				override fun hasNext() : Boolean = source.hasNext()
				override fun next() : R = mapper(source.next())
			}
		}
	}
}

fun <T> Iterable<T>.lookup(predicate: (T) -> Boolean) : T? {
	for (item in this) {
		if (predicate(item)) {
			return item
		}
	}
	return null
}

data class Person(val id: Int, val name: String, val birthYear: Int)

fun main() {
	val inputData = listOf(
		Person(10000, "Afonso Henriques", 1109),
		Person(11000, "In??s de Castro", 1325),
		Person(14000, "Lu??s de Cam??es", 1524),
		Person(12000, "Nuno ??lvares Pereira", 1360),
		Person(13000, "Vasco da Gama", 1469),
		Person(15000, "Josefa de ??bidos", 1630),
		Person(16000, "Fernando Pessoa", 1888),
	)

	/*
	val outputData =
		inputData
			.onEach { println(">> $it") }
			.filter { it.birthYear > 1500 }
			.map { it.name }
			.find { it[0] == 'L' }
	*/
	
	val outputData =
		inputData
			.inspect { println(">> $it") }
			.where   { it.birthYear > 1500 }
			.select  { it.name }
			.lookup  { it[0] == 'L' }
	
	println(outputData)
}

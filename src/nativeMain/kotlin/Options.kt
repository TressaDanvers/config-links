import platform.posix.*

internal fun readAndExecuteOptionsFromArguments(args: Array<out String>) =
	validateArgs(args)
		.onEach { it.onFailure(::fail) }
		.map { it.getOrThrow() }
		.sortedBy { it.priority }
		.let { opts ->
			if (opts.any { opt -> opt.priority == 0u })
				opts.filter { opt -> opt.priority == 0u }
					.sortedBy { opt -> opt.zPriority }
					.distinct()
					.onEach { it.call() }
					.also { exit(EXIT_SUCCESS) }
			else opts
				.also { if (it.none { (opt) -> opt == "t" || opt == "target" }) fail("must provide target path") }
				.also { if (it.none { (opt) -> opt == "s" || opt == "source" }) fail("must provide source path") }
				.onEach { it.call() }
		}

internal fun validateArgs(args: Array<out String>) =
	args.fold((null as String?) to emptyList<Result<Pair<String, String?>>>()) { (opt, l), arg ->
		@Suppress("NAME_SHADOWING")
		if (opt == null) validateOpt(arg).mapCatching { opt ->
			when (opt) {
				in SingleOpt.opts.keys -> null to (l + Result.success(opt to null))
				in DoubleOpt.opts.keys -> opt to l
				else -> {
					err("unrecognized command-line option ${ANSI_BOLD}‘$arg’${ANSI_NOSTRENGTH}")
					throw RuntimeException("unrecognized option")
				}
			}
		}.getOrElse { e -> null to (l + Result.failure(e)) }
		else null to (l + Result.success(opt to arg))
	}.let { (opt, l) ->
		if (opt != null) {
			err("missing argument to ${ANSI_BOLD}‘$opt’${ANSI_NOSTRENGTH}")
			l + Result.failure(RuntimeException("option missing argument"))
		} else l
	}.map { it.map { (opt, arg) ->
		if (arg == null) SingleOpt(opt)
		else DoubleOpt(opt, arg)
	} }

private fun validateOpt(opt: String) = opt.runCatching {
	if (length == 2 && startsWith('-')) substring(1)
	else if (length >= 4 && startsWith("--")) substring(2)
	else if (length == 3 && startsWith("--"))
		throw RuntimeException("`$this` is not a valid option. Did you mean `${substring(1)}`?")
	else throw RuntimeException("`$this` is not a valid option.")
}

internal sealed interface Opt {
	companion object {
		val zPriority = mapOf(
			"v" to 0u, "version" to 0u,
			"h" to 1u, "help" to 1u
		)
	}

	operator fun component1(): String
	val zPriority: UInt get() = Opt.zPriority[component1()] ?: UInt.MAX_VALUE
	val priority: UInt
	fun call()
}

internal data class SingleOpt(val opt: String): Opt {
	companion object {
		val opts = mapOf<String, () -> Unit>(
			"h" to ::help, "help" to ::help,
			"v" to ::version, "version" to ::version
		)
		val priority = mapOf(
			"h" to 0u, "help" to 0u,
			"v" to 0u, "version" to 0u
		)
	}

	override val priority: UInt get() = SingleOpt.priority[opt]!!

	override fun call() { opts[opt]?.let { f -> f() } }
}

internal data class DoubleOpt(val opt: String, val arg: String): Opt {
	companion object {
		val opts = mapOf<String, (String) -> Unit>(
			"s" to ::setSourcePath, "source" to ::setSourcePath,
			"t" to ::setTargetPath, "target" to ::setTargetPath
		)
		val priority = mapOf(
			"s" to 10u, "source" to 10u,
			"t" to 11u, "target" to 11u
		)
	}

	override val priority: UInt get() = DoubleOpt.priority[opt]!!

	override fun call() { opts[opt]?.let { f -> f(arg) } }
}

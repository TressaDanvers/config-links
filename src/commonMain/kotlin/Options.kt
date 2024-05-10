internal fun readAndExecuteOptionsFromArguments(vararg args: String) =
	validateArgs(args)
		.also { opts ->
			opts.filter { it.isFailure }.also { errs ->
				if (errs.size == 1) fail(errs.first().exceptionOrNull()!!)
				else if (errs.isNotEmpty()) fail("multiple errors")
			}
		}.map { it.getOrThrow() }
		.sortedBy { it.priority }
		.distinct()
		.let { opts ->
			if (opts.any { opt -> opt.priority == 0u })
				opts.filter { opt -> opt.priority == 0u }
					.sortedBy { opt -> opt.zPriority }
					.onEach { it.call() }
					.also { exit(ExitSuccess) }
			else opts.onEach { it.call() }
		}

internal fun validateArgs(args: Array<out String>) =
	args.fold((null as String?) to emptyList<Result<Pair<String, String?>>>()) { (opt, l), arg ->
		@Suppress("NAME_SHADOWING")
		if (opt == null) validateOpt(arg).mapCatching { opt ->
			when (opt) {
				null -> {
					throw RuntimeException("invalid option")
				}
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
	else {
		if (length == 3 && startsWith("--") && substring(2) in (SingleOpt.opts.keys + DoubleOpt.opts.keys))
			err(
				"${ANSI_BOLD}‘$this’${ANSI_NOSTRENGTH} is not a valid option;" +
						" did you mean ${ANSI_BOLD}‘${substring(1)}’${ANSI_NOSTRENGTH}?"
			)
		else err("${ANSI_BOLD}‘$this’${ANSI_NOSTRENGTH} is not a valid option")
		null
	}
}

internal sealed interface Opt {
	companion object {
		val zPriority = mapOf(
			"v" to 0u, "version" to 0u,
			"h" to 1u, "help" to 1u
		)
	}

	operator fun component1(): String
	val zPriority: UInt get() = Companion.zPriority[component1()] ?: UInt.MAX_VALUE
	val priority: UInt
	fun call()
}

internal data class SingleOpt(val opt: String): Opt {
	companion object {
		val opts = mapOf<String, () -> Unit>(
			"h" to ::help, "help" to ::help,
			"link" to ::linkAll,
			"v" to ::version, "version" to ::version
		)
		val priority = mapOf(
			"h" to 0u, "help" to 0u,
			"link" to 100u,
			"v" to 0u, "version" to 0u
		)
	}

	override val priority: UInt get() = Companion.priority[opt]!!

	override fun call() { opts[opt]?.let { f -> f() } }
}

internal data class DoubleOpt(val opt: String, val arg: String): Opt {
	companion object {
		val opts = mapOf<String, (String) -> Unit>(
      "m" to ::manage, "manage" to ::manage,
      "s" to ::setSourcePath, "source" to ::setSourcePath,
			"t" to ::setTargetPath, "target" to ::setTargetPath
		)
		val priority = mapOf(
      "m" to 50u, "manage" to 50u,
      "s" to 10u, "source" to 10u,
			"t" to 11u, "target" to 11u
		)
	}

	override val priority: UInt get() = Companion.priority[opt]!!

	override fun call() { opts[opt]?.let { f -> f(arg) } }
}

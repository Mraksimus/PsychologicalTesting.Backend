package ru.psychologicalTesting.main.utils

import dev.nesk.akkurate.validatables.Validatable

inline fun <reified R> Validatable<*>.ifInstanceOf(
    block: Validatable<R>.() -> Unit
) {

    val value = unwrap()
    if (value is R) {
        block(withValue(value))
    }

}

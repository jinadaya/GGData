package com.royaal.entities

interface Mapper <in I, out O> {

    fun map(input: I): O
}
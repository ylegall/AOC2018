package aoc2018

import aoc2018.Day22.Gear.CLIMBING
import aoc2018.Day22.Gear.NONE
import aoc2018.Day22.Gear.TORCH
import util.Direction
import util.Point
import util.enumSetOf
import util.mDist
import util.move
import java.util.PriorityQueue

private object Day22 {

    private const val DEPTH = 11541

    private enum class Gear { TORCH, CLIMBING, NONE }

    private class Cave(
            val targetPoint: Point,
            val depth: Int = DEPTH
    ) {
        val startPoint = Point(0, 0)
        val erosionLevels = HashMap<Point, Int>()
        val geoIndex = HashMap<Point, Int>()

        init {
            geoIndex[startPoint] = 0
            geoIndex[targetPoint] = 0
            getErosionLevel(targetPoint)
        }

        fun getGeoIndex(point: Point): Int = geoIndex.getOrPut(point) {
            when {
                point.y == 0 -> point.x * 16807
                point.x == 0 -> point.y * 48271
                else -> getErosionLevel(point.copy(x = point.x - 1)) *
                        getErosionLevel(point.copy(y = point.y - 1))
            }
        }

        fun getErosionLevel(point: Point): Int = erosionLevels.getOrPut(point) {
            (getGeoIndex(point) + depth) % 20183
        }

        fun riskLevel() = (startPoint.x .. targetPoint.x).flatMap { x ->
            (startPoint.y .. targetPoint.y).map { y ->
                terrainType(Point(x, y))
            }
        }.sum()

        private fun terrainType(point: Point) = getErosionLevel(point) % 3

        private data class State(val pos: Point, val gear: Gear) {
            var steps = 0
        }

        private fun validGear(point: Point) = when (terrainType(point)) {
            0 -> enumSetOf(TORCH, CLIMBING)
            1 -> enumSetOf(CLIMBING, NONE)
            2 -> enumSetOf(TORCH, NONE)
            else -> throw Exception("invalid region type")
        }

        private fun State.isValid() = pos.x >= 0 && pos.y >= 0 && gear in validGear(pos)

        private fun State.nextStates(): Collection<State> {
            val currentSteps = steps
            val gearOptions = Gear.values().toList() - gear
            val neighbors = Direction.values().map { pos.move(it) }

            val nextStatesWithSameGear = neighbors.map { newPos ->
                State(newPos, gear).apply { steps = currentSteps + 1 }
            }.filter { it.isValid() }

            val nextStatesWithNewGear = neighbors.flatMap { newPos ->
                gearOptions.map { newGear ->
                    State(newPos, newGear).apply { steps = currentSteps + 8 }
                }
            }.filter { it.isValid() }

            return nextStatesWithNewGear + nextStatesWithSameGear
        }

        fun findMinStepsToTarget(): Int {
            val seenStates = HashSet<State>()
            val frontier = PriorityQueue<State>(compareBy { it.steps + it.pos.mDist(targetPoint) })
            frontier.add(State(startPoint, TORCH))
            while (frontier.isNotEmpty()) {
                val nextState = frontier.poll()
                if (nextState.pos == targetPoint && nextState.gear == TORCH) {
                    return nextState.steps
                }
                seenStates.add(nextState)
                frontier.addAll(nextState.nextStates().filter { it !in seenStates })
            }
            throw Exception("goal not found")
        }

        fun print() {
            for (y in startPoint.y .. targetPoint.y) {
                for (x in startPoint.x .. targetPoint.x) {
                    print(when (terrainType(Point(x, y))) {
                        0 -> "."
                        1 -> "="
                        2 -> "|"
                        else -> throw Exception("invalid region type")
                    })
                }
                println()
            }
        }
    }

    fun run() {
//        val cave = Cave(Point(10, 10), 510)
        val cave = Cave(Point(14, 778))
        //cave.print()
        //println(cave.riskLevel())
        println(cave.findMinStepsToTarget())
    }
}

fun main() {
    Day22.run()
}

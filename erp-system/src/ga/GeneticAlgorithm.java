package ga;

import model.*;

import java.util.*;

public class GeneticAlgorithm {

    private List<Schedule> population;
    private int populationSize = 50;

    public GeneticAlgorithm(List<Schedule> initialPopulation) {
        this.population = initialPopulation;
    }

    public Schedule evolve() {

        for (int generation = 0; generation < 200; generation++) {

            population.sort(Comparator.comparingInt(FitnessCalculator::calculate));

            if (FitnessCalculator.calculate(population.get(0)) == 0) {
                break;
            }

            List<Schedule> newPopulation = new ArrayList<>();

            for (int i = 0; i < populationSize / 2; i++) {
                Schedule parent1 = population.get(i);
                Schedule parent2 = population.get(i + 1);

                Schedule child = crossover(parent1, parent2);
                mutate(child);

                newPopulation.add(child);
            }

            population = newPopulation;
        }

        population.sort(Comparator.comparingInt(FitnessCalculator::calculate));
        return population.get(0);
    }

    private Schedule crossover(Schedule p1, Schedule p2) {
        Schedule child = new Schedule();

        int count = 0;
        for (TimeSlot slot : p1.getAssignments().keySet()) {
            if (count % 2 == 0)
                child.assign(slot, p1.getAssignments().get(slot));
            else
                child.assign(slot, p2.getAssignments().get(slot));
            count++;
        }

        return child;
    }

    private void mutate(Schedule schedule) {
        Random rand = new Random();
        List<TimeSlot> slots = new ArrayList<>(schedule.getAssignments().keySet());
        if (slots.size() < 2) {
            return;
        }

        if (rand.nextDouble() < 0.1) {
            TimeSlot randomSlot = slots.get(rand.nextInt(slots.size()));
            TimeSlot otherSlot = slots.get(rand.nextInt(slots.size()));
            while (otherSlot.equals(randomSlot)) {
                otherSlot = slots.get(rand.nextInt(slots.size()));
            }

            Assignment first = schedule.getAssignments().get(randomSlot);
            Assignment second = schedule.getAssignments().get(otherSlot);
            schedule.assign(randomSlot, second);
            schedule.assign(otherSlot, first);
        }
    }
}

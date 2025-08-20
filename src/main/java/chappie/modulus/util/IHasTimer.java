package chappie.modulus.util;

import net.minecraft.util.Mth;

import java.util.function.Supplier;

public interface IHasTimer {

    Iterable<Timer> timers();

    class Cooldown extends Timer {
        public Cooldown() {
            super(() -> 0, () -> false);
        }

        @Override
        public void update() {
            this.prevTimer = this.timer;
            if (this.timer > 0) {
                this.timer--;
            }
        }

        public void start(int timer) {
            this.maxTimer = () -> timer;
            this.timer = timer;
        }

        public boolean end() {
            return this.prevTimer == 0 && this.timer == 0;
        }
    }

    class Timer {
        public int prevTimer, timer;
        public Supplier<Integer> maxTimer;
        public Supplier<Boolean> predicate;

        public Timer(Supplier<Integer> maxTimer, Supplier<Boolean> predicate) {
            this.maxTimer = maxTimer;
            this.predicate = predicate;
        }

        public void update() {
            int maxTimer = this.maxTimer.get();
            boolean predicate = this.predicate.get();
            this.prevTimer = this.timer;
            if (this.timer < maxTimer && predicate) {
                this.timer++;
            }
            if (this.timer > 0 && !predicate) {
                this.timer--;
            }
        }

        public float value(float partialTicks) {
            return Mth.lerp(partialTicks, this.prevTimer, this.timer) / this.maxTimer.get();
        }

    }
}

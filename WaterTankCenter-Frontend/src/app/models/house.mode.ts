import { WaterPipeDto } from "./water-pipe.mode";
import { WaterTank } from "./water-tank.model";

export interface HouseDto {
  id: number;
  waterTank?: WaterTank,
  waterPipe: WaterPipeDto
}
